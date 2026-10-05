package gg.flyte.pluginportal.plugin.network

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.runtime.PortalRuntime
import gg.flyte.pluginportal.common.types.SocketActions
import gg.flyte.pluginportal.common.types.enums.ServerType
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.HttpInfo
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URI
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermissions
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
    private val credentialFile = File(runtime.dataFolder, "network-node.json")
    private val journalFile = File(runtime.dataFolder, "network-operations.json")
    private val journal = linkedMapOf<String, JsonObject>()
    @Volatile private var identity: JsonObject? = null
    @Volatile private var socket: WebSocket? = null
    @Volatile var status: String = "Not enrolled"
        private set
    private var retrySeconds = 2L
    private val generation = AtomicLong(0)
    @Volatile private var ready = false
    val isStopped get() = operations.isTerminated
    val nodeId get() = identity?.get("nodeId")?.asString
    val role get() = identity?.get("role")?.asString
    private val platform get() = if (ServerType.VELOCITY in runtime.serverTypes) "velocity" else "bukkit"

    fun start() {
        try {
            if (journalFile.exists()) {
                requireSafeFile(journalFile)
                require(journalFile.length() <= 4 * 1024 * 1024) { "Journal too large" }
                val entries = JsonParser.parseString(journalFile.readText()).asJsonObject
                require(entries.size() <= 1000) { "Journal capacity exceeded" }
                for ((id, value) in entries.entrySet()) {
                    require(validId(id)) { "Invalid journal" }
                    val entry = value.asJsonObject
                    if (entry.getAsJsonObject("result")?.get("status")?.asString == "executing") {
                        entry.add("result", result("unknown", "Execution interrupted; inspect installed files before retrying."))
                    }
                    journal[id] = entry
                }
                saveJournal()
            }
            if (credentialFile.exists()) {
                requireSafeFile(credentialFile)
                require(credentialFile.length() < 4096) { "Invalid credential file" }
                val loaded = JsonParser.parseString(credentialFile.readText()).asJsonObject
                require(Regex("^ppn_[A-Za-z0-9_-]{43}$").matches(loaded.get("credential").asString))
                require(validId(loaded.get("nodeId").asString) && validId(loaded.get("networkId").asString))
                identity = loaded
                scheduler.execute { connect() }
            }
        } catch (_: Exception) {
            status = "Network files could not be loaded; enrollment is disabled until they are repaired"
            runtime.logger.warning(status)
            ready = false
            return
        }
        ready = true
    }

    @Synchronized fun enroll(code: String) {
        check(ready && !closed.get()) { "Network client is unavailable" }
        check(identity == null) { "Already enrolled. Revoke the old node in the dashboard and run network leave first." }
        require(Regex("^[A-Za-z0-9_-]{43}$").matches(code)) { "Invalid enrollment code" }
        val response = request("enroll", jsonObject("code" to code, "platform" to platform), authenticated = false)
        require(Regex("^ppn_[A-Za-z0-9_-]{43}$").matches(response.get("credential").asString))
        privateWrite(credentialFile, response)
        identity = response
        status = "Enrolled; connecting"
        scheduler.execute { connect() }
    }

    @Synchronized fun leave() {
        // Removing a local credential does not grant permission to revoke a different node.
        generation.incrementAndGet()
        identity = null
        socket?.close(1000, "Local disconnect")
        socket = null
        requireSafeFile(credentialFile)
        Files.deleteIfExists(credentialFile.toPath())
        status = "Not enrolled; revoke the old node in the dashboard"
    }

    fun state(): JsonObject = request("state")
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
                403 -> "An active network subscription and controller permission are required"
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
                                try { operations.execute { execute(operation) } }
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

    private fun execute(operation: JsonObject) {
        val id = operation.get("id")?.asString ?: return
        if (!validId(id)) return
        val action = operation.getAsJsonObject("action") ?: return
        val fingerprint = canonicalAction(action) ?: return
        val expiresAt = operation.get("expiresAt")?.asLong ?: return
        val existing = journal[id]
        if (existing != null) {
            if (existing.get("action").asString == fingerprint) sendResult(id, existing.getAsJsonObject("result"))
            return
        }
        if (closed.get() || identity == null) { sendResult(id, result("skipped", "Node disconnected before execution")); return }
        if (expiresAt <= System.currentTimeMillis() || expiresAt > System.currentTimeMillis() + 310_000) {
            sendResult(id, result("skipped", "Operation expired before execution")); return
        }
        journal.entries.removeIf { (_, entry) -> entry.get("createdAt").asLong < System.currentTimeMillis() - 30L * 86400_000 && entry.getAsJsonObject("result").get("status").asString != "executing" }
        if (journal.size >= 1000) { sendResult(id, result("failed", "Local operation journal capacity reached")); return }
        val entry = JsonObject().apply { addProperty("action", fingerprint); addProperty("createdAt", System.currentTimeMillis()); add("result", result("executing")) }
        journal[id] = entry
        try {
            saveJournal() // The durable reservation must precede filesystem changes.
            sendResult(id, entry.getAsJsonObject("result"))
            val kind = action.get("kind").asString
            val outcome = if (kind == "inventory") result("succeeded").apply { add("inventory", inventory()) }
            else {
                val actions = SocketActions()
                val response = when (kind) {
                    "install" -> actions.install.run(action)
                    "update" -> actions.update.run(action)
                    "uninstall" -> actions.uninstall.run(action)
                    else -> error("Unsupported operation")
                }
                if (response.success && response.message == "Plugin is already up to date") result("succeeded", response.message)
                else if (response.success) result("staged", "Files changed; restart this node to apply the change.")
                else result("failed", response.message ?: "Operation failed")
            }
            entry.add("result", outcome)
            saveJournal()
            sendResult(id, outcome)
            publishInventory()
        } catch (failure: Exception) {
            runtime.logger.warning("Network operation interrupted: ${failure.javaClass.simpleName} at ${failure.stackTrace.take(5).joinToString()}")
            val outcome = result("unknown", "Operation interrupted; inspect installed files before retrying.")
            entry.add("result", outcome)
            runCatching { saveJournal() }
            sendResult(id, outcome)
        }
    }
    private fun canonicalAction(action: JsonObject): String? = runCatching {
        val keys = setOf("kind", "platform", "id", "version", "channel")
        require(action.keySet().all { it in keys })
        val kind = action.get("kind").asString
        require(kind in setOf("inventory", "install", "update", "uninstall"))
        if (kind != "inventory") {
            require(action.get("platform").asString in setOf("MODRINTH", "HANGAR", "SPIGOTMC", "POLYMART"))
            require(Regex("^[a-zA-Z0-9_.-]{1,100}$").matches(action.get("id").asString))
        }
        action.get("version")?.let { require(it.asString.length in 1..100) }
        action.get("channel")?.let { require(it.asString in setOf("release", "beta", "alpha")) }
        JsonObject().apply { for (key in keys.sorted()) action.get(key)?.let { add(key, it) } }.toString()
    }.getOrNull()

    private fun inventory() = JsonObject().apply {
        addProperty("platform", platform)
        addProperty("pluginVersion", runtime.description.version.take(120))
        addProperty("minecraftVersion", runtime.server.minecraftVersion.take(120))
        val managed = LocalPluginCache.map { jsonObject("name" to it.name.take(120), "version" to it.version.take(120), "platform" to it.platform.name, "id" to it.platformId.take(120)) }
        val names = managed.map { it.get("name").asString.lowercase() }.toSet()
        val other = runtime.server.plugins.filter { it.name.lowercase() !in names }.map { jsonObject("name" to it.name.take(120), "version" to it.description.version.take(120)) }
        add("plugins", GSON.toJsonTree((managed + other).take(500)))
    }
    private fun publishInventory(target: WebSocket? = socket) { target?.send(JsonObject().apply { addProperty("v", 1); addProperty("type", "inventory"); add("inventory", inventory()) }.toString()) }
    private fun sendResult(id: String, result: JsonObject) { socket?.send(JsonObject().apply { addProperty("v", 1); addProperty("type", "result"); addProperty("id", id); add("result", result) }.toString()) }
    private fun result(status: String, message: String? = null) = JsonObject().apply { addProperty("status", status); message?.let { addProperty("message", it.take(500)) } }
    private fun saveJournal() = privateWrite(journalFile, JsonObject().apply { for ((id, entry) in journal) add(id, entry) })
    private fun requireSafeFile(file: File) { require(!Files.isSymbolicLink(file.toPath()) && (!file.exists() || Files.isRegularFile(file.toPath(), LinkOption.NOFOLLOW_LINKS))) { "Unsafe network file" } }
    private fun privateWrite(file: File, value: JsonObject) {
        requireSafeFile(file)
        runtime.dataFolder.mkdirs()
        val temporary = Files.createTempFile(runtime.dataFolder.toPath(), ".network-", ".json")
        try {
            if (Files.getFileStore(temporary).supportsFileAttributeView("posix")) Files.setPosixFilePermissions(temporary, PosixFilePermissions.fromString("rw-------"))
            FileChannel.open(temporary, StandardOpenOption.WRITE).use { channel ->
                val buffer = java.nio.ByteBuffer.wrap(value.toString().toByteArray(Charsets.UTF_8))
                while (buffer.hasRemaining()) channel.write(buffer)
                channel.force(true)
            }
            Files.move(temporary, file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally { Files.deleteIfExists(temporary) }
    }
    private fun validId(value: String) = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$").matches(value)

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
