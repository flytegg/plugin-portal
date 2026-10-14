package gg.flyte.pluginportal.plugin.network

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.runtime.PortalRuntime
import gg.flyte.pluginportal.common.types.enums.ServerType
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.HttpInfo
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.util.UUID
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/** The API owns authorization. This client only receives operations for its own node. */
class NetworkClient(private val runtime: PortalRuntime) : AutoCloseable {
    private val transport = OkHttpClient.Builder().callTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(false).followSslRedirects(false).build()
    private val scheduler = ScheduledThreadPoolExecutor(1) { Thread(it, "PluginPortal-network-link").apply { isDaemon = true } }
        .apply { removeOnCancelPolicy = true }
    private val operations = java.util.concurrent.ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
        java.util.concurrent.ArrayBlockingQueue(32),
        { Thread(it, "PluginPortal-network-operation").apply { isDaemon = true } })
    private val closed = AtomicBoolean(false)
    private val store = NetworkStateStore(runtime.dataFolder)
    private val enrollment = NetworkEnrollment({ platform }) { route, body -> request(route, body, authenticated = false) }
    private val executor = NetworkOperationExecutor(runtime, store, { nodeId }, { closed.get() },
        { inventory() }, { id, result -> sendResult(id, result) }, { publishInventory() })
    @Volatile private var identity: JsonObject? = null
    @Volatile private var socket: WebSocket? = null
    @Volatile var status: String = "Not enrolled"
        private set
    private var retrySeconds = 2L
    private val generation = AtomicLong(0)
    private data class ServerNames(val nodeId: String, val receivedAt: Long, val values: List<String>)
    @Volatile private var serverNames: ServerNames? = null
    private val refreshingNames = AtomicBoolean(false)
    @Volatile private var ready = false
    val isStopped get() = operations.isTerminated
    val nodeId get() = identity?.get("nodeId")?.asString
    val role get() = identity?.get("role")?.asString
    private val platform get() = if (ServerType.VELOCITY in runtime.serverTypes) "velocity" else "bukkit"

    fun start() {
        try {
            executor.load()
            identity = store.readIdentity()
            if (identity != null) scheduler.execute { connect() }
        } catch (_: Exception) {
            status = "Network files could not be loaded; enrollment is disabled until they are repaired"
            runtime.logger.warning(status)
            ready = false
            return
        }
        ready = true
    }

    @Synchronized fun reviewEnrollment(code: String): EnrollmentReview {
        check(ready && !closed.get()) { "Network client is unavailable" }
        check(identity == null) { "Already enrolled. Revoke the old node in the dashboard and run unlink first." }
        return enrollment.review(code)
    }

    @Synchronized fun confirmEnrollment() {
        check(ready && !closed.get() && identity == null) { "Network client is unavailable or already linked" }
        val response = enrollment.confirm()
        require(Regex("^ppn_[A-Za-z0-9_-]{43}$").matches(response.get("credential").asString))
        require(validId(response.get("nodeId").asString) && validId(response.get("networkId").asString))
        Config.setRemoteWrites(false)
        store.saveIdentity(response)
        identity = response
        status = "Enrolled; connecting"
        scheduler.execute { connect() }
    }

    fun cancelEnrollment() = enrollment.cancel()

    @Synchronized fun leave() {
        // Removing a local credential does not grant permission to revoke a different node.
        generation.incrementAndGet()
        Config.setRemoteWrites(false)
        enrollment.cancel()
        identity = null
        socket?.close(1000, "Local disconnect")
        socket = null
        store.deleteIdentity()
        status = "Not enrolled; revoke the old node in the dashboard"
    }

    fun state(): JsonObject {
        val id = nodeId
        val response = request("state")
        if (id != null && nodeId == id) {
            val names = response.getAsJsonArray("nodes").map { it.asJsonObject }.filter { !it.get("revoked").asBoolean }
                .flatMap { listOf(it.get("name").asString, it.get("id").asString) }.distinct()
            serverNames = ServerNames(id, System.nanoTime(), names)
        }
        return response
    }

    fun serverNameSuggestions(): List<String> {
        val id = nodeId ?: return emptyList()
        if (role != "controller" || closed.get()) return emptyList()
        val cached = serverNames?.takeIf { it.nodeId == id }
        val age = cached?.let { System.nanoTime() - it.receivedAt } ?: Long.MAX_VALUE
        // Bukkit completion is synchronous; refresh without blocking its server thread.
        if (age > TimeUnit.SECONDS.toNanos(10) && refreshingNames.compareAndSet(false, true)) {
            runtime.executor.execute {
                try { state() } catch (_: Exception) { serverNames = null }
                finally { refreshingNames.set(false) }
            }
        }
        return cached?.takeIf { age <= TimeUnit.SECONDS.toNanos(30) }?.values ?: emptyList()
    }
    fun submit(targets: List<String>, action: JsonObject): JsonObject {
        check(role == "controller") { "This node is not a controller" }
        require(targets.isNotEmpty() && targets.size <= 100 && targets.distinct().size == targets.size && targets.all(::validId)) { "Use 1 to 100 unique node IDs" }
        val input = JsonObject().apply {
            addProperty("id", UUID.randomUUID().toString())
            add("targets", GSON.toJsonTree(targets))
            add("action", action)
        }
        return request("operations", input)
    }

    private fun request(route: String, data: JsonObject? = null, authenticated: Boolean = true): JsonObject {
        val base = HttpInfo.getApiBaseUrl().trimEnd('/')
        val builder = Request.Builder().url("$base/network/node/$route")
        if (authenticated) builder.header("Authorization", "Bearer ${identity?.get("credential")?.asString ?: error("Enroll this node first")}")
        if (data != null || route == "connect") builder.post((data?.toString() ?: "{}").toRequestBody("application/json".toMediaType()))
        transport.newCall(builder.build()).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException(when (response.code) {
                401 -> "Node credential or enrollment code is invalid, expired, or revoked"
                403 -> "This action requires current account access, a controller role, and console write approval on each target"
                400 -> "Review the enrollment again, or check the plugin and target servers"
                429 -> "Network service rate or capacity limit reached"
                else -> "Network service request failed (${response.code}); try again later"
            })
            val source = response.body?.source() ?: error("Empty network response")
            if (source.request(2 * 1024 * 1024L + 1)) error("Network response too large")
            return JsonParser.parseString(source.readUtf8()).asJsonObject
        }
    }

    private fun connect() {
        if (closed.get() || identity == null) return
        val current = generation.incrementAndGet()
        try {
            status = "Connecting"
            val session = request("connect")
            if (closed.get() || current != generation.get() || identity == null) return
            check(session.get("nodeId").asString == nodeId && session.get("networkId").asString == identity?.get("networkId")?.asString)
            val url = session.get("websocketUrl").asString
            val uri = URI(url)
            require(uri.userInfo == null && uri.query == null && uri.fragment == null && (uri.scheme == "wss" || (System.getProperty("pluginportal.dev", "false").toBoolean() && uri.scheme == "ws" && uri.host in setOf("localhost", "127.0.0.1", "host.docker.internal")))) { "Unsafe relay URL" }
            val expiresAt = session.get("expiresAt").asLong
            require(expiresAt > System.currentTimeMillis() && expiresAt <= System.currentTimeMillis() + 310_000)
            socket?.close(1000, "Session renewed")
            socket = transport.newWebSocket(Request.Builder().url(url).header("Authorization", "Bearer ${session.get("ticket").asString}").build(), object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    if (closed.get() || current != generation.get()) { webSocket.close(1000, "Superseded"); return }
                    retrySeconds = 2
                    status = "Connected"
                    publishInventory(webSocket)
                }
                override fun onMessage(webSocket: WebSocket, text: String) {
                    if (current != generation.get() || closed.get()) return
                    if (text == "pong") return
                    if (text.toByteArray().size > 256 * 1024) { webSocket.close(1008, "Message too large"); return }
                    try {
                        val message = JsonParser.parseString(text).asJsonObject
                        require(message.get("v").asInt == 1)
                        when (message.get("type").asString) {
                            "ready" -> require(message.get("nodeId").asString == nodeId)
                            "operation" -> {
                                val operation = message.getAsJsonObject("operation").deepCopy()
                                val expectedNodeId = nodeId ?: return
                                try { operations.execute { executor.execute(operation, expectedNodeId) } }
                                catch (_: java.util.concurrent.RejectedExecutionException) {
                                    operation.get("id")?.asString?.takeIf(::validId)?.let { sendResult(it, result("failed", "Node operation queue is full; submit a new operation later.")) }
                                }
                            }
                            else -> error("Invalid message")
                        }
                    } catch (_: Exception) { webSocket.close(1008, "Invalid relay message") }
                }
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) = reconnect(current)
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) = reconnect(current)
                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) { webSocket.close(code, reason) }
            })
            scheduler.schedule({ if (current == generation.get()) connect() }, maxOf(1, expiresAt - System.currentTimeMillis() - 60_000), TimeUnit.MILLISECONDS)
            scheduler.schedule({ if (current == generation.get()) heartbeat(current) }, 30, TimeUnit.SECONDS)
        } catch (_: Exception) { reconnect(current) }
    }
    private fun heartbeat(current: Long) {
        if (closed.get() || current != generation.get()) return
        socket?.send("ping")
        publishInventory()
        scheduler.schedule({ heartbeat(current) }, 30, TimeUnit.SECONDS)
    }
    private fun reconnect(current: Long) {
        if (closed.get() || !generation.compareAndSet(current, current + 1)) return
        status = "Disconnected; retrying enrollment authorization"
        // Fence duplicate failure/close callbacks so each session schedules one retry.
        val retryGeneration = current + 1
        val delay = retrySeconds + java.util.concurrent.ThreadLocalRandom.current().nextLong(0, 3)
        retrySeconds = minOf(60, retrySeconds * 2)
        scheduler.schedule({ if (retryGeneration == generation.get()) connect() }, delay, TimeUnit.SECONDS)
    }

    private fun inventory() = JsonObject().apply {
        addProperty("platform", platform)
        addProperty("pluginVersion", runtime.description.version.take(120))
        addProperty("minecraftVersion", runtime.server.minecraftVersion.take(120))
        addProperty("remoteControl", Config.allowsRemoteWrites())
        addProperty("observedAt", System.currentTimeMillis())
        val managed = LocalPluginCache.map { plugin ->
            jsonObject("name" to plugin.name.take(120), "version" to plugin.version.take(120), "platform" to plugin.platform.name, "id" to plugin.platformId.take(120)).apply {
                runtime.server.plugins.firstOrNull { it.name.equals(plugin.name, ignoreCase = true) }?.description?.version?.let { addProperty("runningVersion", it.take(120)) }
            }
        }
        val names = managed.map { it.get("name").asString.lowercase() }.toSet()
        val other = runtime.server.plugins.filter { it.name.lowercase() !in names }.map { jsonObject("name" to it.name.take(120), "version" to it.description.version.take(120), "runningVersion" to it.description.version.take(120)) }
        add("plugins", GSON.toJsonTree((managed + other).take(500)))
    }
    private fun publishInventory(target: WebSocket? = socket) { target?.send(JsonObject().apply { addProperty("v", 1); addProperty("type", "inventory"); add("inventory", inventory()) }.toString()) }
    fun refreshInventory() = publishInventory()
    private fun sendResult(id: String, result: JsonObject) { socket?.send(JsonObject().apply { addProperty("v", 1); addProperty("type", "result"); addProperty("id", id); add("result", result) }.toString()) }
    private fun result(status: String, message: String? = null) = JsonObject().apply { addProperty("status", status); message?.let { addProperty("message", it.take(500)) } }
    private fun validId(value: String) = NetworkStateStore.validId(value)

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        generation.incrementAndGet()
        socket?.close(1000, "Node stopping")
        scheduler.shutdownNow()
        operations.shutdown()
        if (!operations.awaitTermination(15, TimeUnit.SECONDS)) operations.shutdownNow()
        transport.dispatcher.executorService.shutdown()
        transport.connectionPool.evictAll()
    }
    companion object {
        fun jsonObject(vararg values: Pair<String, String>) = JsonObject().apply { values.forEach { (key, value) -> addProperty(key, value) } }
    }
}
