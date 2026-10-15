package gg.flyte.pluginportal.plugin.network

import com.google.gson.JsonObject
import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.runtime.PortalRuntime
import gg.flyte.pluginportal.common.types.SocketActions
import gg.flyte.pluginportal.common.util.isPluginPortal

/** Serial execution, durable reservations, and recovery are separate from socket transport. */
internal class NetworkOperationExecutor(
    private val runtime: PortalRuntime,
    private val store: NetworkStateStore,
    private val nodeId: () -> String?,
    private val closed: () -> Boolean,
    private val inventory: () -> JsonObject,
    private val sendResult: (String, JsonObject) -> Unit,
    private val publishInventory: () -> Unit,
) {
    private val journal = linkedMapOf<String, JsonObject>()

    fun load() {
        val entries = store.readJournal() ?: return
        require(entries.size() <= 1000) { "Journal capacity exceeded" }
        for ((id, value) in entries.entrySet()) {
            require(validId(id)) { "Invalid journal" }
            val entry = value.asJsonObject
            require(entry.get("action").isJsonPrimitive && entry.get("createdAt").asLong > 0) { "Invalid journal entry" }
            if (entry.getAsJsonObject("result").get("status").asString == "executing") {
                entry.add("result", result("unknown", "Execution interrupted; inspect installed files before retrying."))
            }
            journal[id] = entry
        }
        saveJournal()
    }

    fun execute(operation: JsonObject, expectedNodeId: String) {
        // A queued operation must never cross a local leave and re-enrollment.
        if (closed() || nodeId() != expectedNodeId) return
        val targets = operation.getAsJsonArray("targets")?.map { it.asString } ?: return
        if (targets.size !in 1..100 || targets.distinct().size != targets.size || expectedNodeId !in targets || !targets.all(::validId)) return
        val id = operation.get("id")?.asString ?: return
        if (!validId(id)) return
        val action = operation.getAsJsonObject("action") ?: return
        val source = operation.get("source")?.asString ?: "dashboard"
        if (source !in setOf("dashboard", "controller")) return
        if (action.get("kind")?.asString != "inventory" && !Config.allowsRemoteWrites()) {
            sendResult(id, result("failed", "Remote plugin control is disabled. Run pp control enable in this console first."))
            return
        }
        if (action.get("kind")?.asString != "inventory" && LocalPluginCache.any {
            it.isPluginPortal && it.platform.name == action.get("platform")?.asString && it.platformId == action.get("id")?.asString
        }) {
            sendResult(id, result("failed", "Manage Plugin Portal itself with the local upgrade command."))
            return
        }
        val fingerprint = (canonicalAction(action) ?: return) + ":" + source
        val expiresAt = operation.get("expiresAt")?.asLong ?: return
        val existing = journal[id]
        if (existing != null) {
            if (existing.get("action").asString == fingerprint) sendResult(id, existing.getAsJsonObject("result"))
            return
        }
        if (closed() || nodeId() == null) { sendResult(id, result("skipped", "Node disconnected before execution")); return }
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
                if (!Config.allowsRemoteWrites() || nodeId() != expectedNodeId) {
                    val rejected = result("failed", "Remote control was disabled or unlinked before execution")
                    entry.add("result", rejected)
                    saveJournal()
                    sendResult(id, rejected)
                    return
                }
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
        val keys = setOf("kind", "platform", "id", "version", "channel", "ignoreMinecraftVersion")
        require(action.keySet().all { it in keys })
        val kind = action.get("kind").asString
        require(kind in setOf("inventory", "install", "update", "uninstall"))
        if (kind != "inventory") {
            require(action.get("platform").asString in setOf("MODRINTH", "HANGAR", "SPIGOTMC", "POLYMART"))
            require(Regex("^[a-zA-Z0-9_.-]{1,100}$").matches(action.get("id").asString))
        }
        action.get("version")?.let { require(it.asString.length in 1..100) }
        action.get("channel")?.let { require(it.asString in setOf("release", "beta", "alpha")) }
        action.get("ignoreMinecraftVersion")?.let {
            require(it.isJsonPrimitive && it.asJsonPrimitive.isBoolean)
            require(kind in setOf("install", "update") && action.has("version"))
        }
        JsonObject().apply { for (key in keys.sorted()) action.get(key)?.let { add(key, it) } }.toString()
    }.getOrNull()

    private fun saveJournal() = store.saveJournal(JsonObject().apply { for ((id, entry) in journal) add(id, entry) })
    private fun validId(value: String) = NetworkStateStore.validId(value)
    private fun result(status: String, message: String? = null) = JsonObject().apply {
        addProperty("status", status)
        message?.let { addProperty("message", it.take(500)) }
    }
}
