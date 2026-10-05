package gg.flyte.pluginportal.plugin.network

import gg.flyte.pluginportal.common.chat.sendFailure
import gg.flyte.pluginportal.common.commands.lamp.CommandPermission
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import gg.flyte.pluginportal.plugin.PortalApplication
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.Optional

@Command("pp network", "ppnetwork")
@CommandPermission("pluginportal.network")
class NetworkCommands {
    private val client get() = PortalApplication.network

    @Subcommand("enroll")
    fun enroll(actor: PortalCommandActor, @Named("code") code: String) {
        if (!actor.isConsole) { actor.audience.sendFailure("Run enrollment in the server or proxy console."); return }
        work(actor) { client.enroll(code); "Node enrolled. ${client.status}." }
    }
    @Subcommand("leave")
    fun leave(actor: PortalCommandActor) {
        if (!actor.isConsole) { actor.audience.sendFailure("Run network leave in the console."); return }
        work(actor) { client.leave(); client.status }
    }
    @Subcommand("status")
    fun status(actor: PortalCommandActor) { actor.sendRawMessage("Network: ${client.status}. Node: ${client.nodeId ?: "none"}. Role: ${client.role ?: "none"}.") }
    @Subcommand("list")
    fun list(actor: PortalCommandActor) = work(actor) {
        client.state().getAsJsonArray("nodes").joinToString("\n") { node ->
            val value = node.asJsonObject
            "${value.get("id").asString}  ${value.get("name").asString} (${value.get("platform").asString}) ${if (value.get("online").asBoolean) "online" else "offline"}${if (value.get("revoked").asBoolean) " revoked" else ""}"
        }.ifEmpty { "No enrolled nodes" }
    }
    @Subcommand("operations")
    fun operations(actor: PortalCommandActor) = work(actor) {
        client.state().getAsJsonArray("operations").take(20).joinToString("\n") { value ->
            val operation = value.asJsonObject
            "${operation.get("id").asString} ${operation.getAsJsonObject("action").get("kind").asString}\n" +
                operation.getAsJsonObject("results").entrySet().joinToString("\n") { (id, result) ->
                    val outcome = result.asJsonObject
                    "$id: ${outcome.get("status").asString}${outcome.get("message")?.asString?.let { " - $it" } ?: ""}"
                }
        }.ifEmpty { "No recent operations" }
    }
    @Subcommand("refresh")
    fun refresh(actor: PortalCommandActor, @Named("nodeIds") targetIds: String) = operate(actor, targetIds, NetworkClient.jsonObject("kind" to "inventory"))
    @Subcommand("install")
    fun install(actor: PortalCommandActor, @Named("nodeIds") targetIds: String, @Named("platform") platform: String, @Named("pluginId") pluginId: String,
                @Optional @Named("version") version: String?) = operate(actor, targetIds, action("install", platform, pluginId, version))
    @Subcommand("update")
    fun update(actor: PortalCommandActor, @Named("nodeIds") targetIds: String, @Named("platform") platform: String, @Named("pluginId") pluginId: String,
               @Optional @Named("version") version: String?) = operate(actor, targetIds, action("update", platform, pluginId, version))
    @Subcommand("uninstall")
    fun uninstall(actor: PortalCommandActor, @Named("nodeIds") targetIds: String, @Named("platform") platform: String, @Named("pluginId") pluginId: String) = operate(actor, targetIds, action("uninstall", platform, pluginId, null))
    private fun action(kind: String, platform: String, id: String, version: String?) = NetworkClient.jsonObject("kind" to kind, "platform" to platform.uppercase(), "id" to id).apply { version?.let { addProperty("version", it) } }
    private fun operate(actor: PortalCommandActor, targetIds: String, action: com.google.gson.JsonObject) = work(actor) {
        val operation = client.submit(targetIds.split(','), action)
        "Operation ${operation.get("id").asString}\n" + operation.getAsJsonObject("results").entrySet().joinToString("\n") { (id, value) -> "$id: ${value.asJsonObject.get("status").asString}" } + "\nUse network operations or the dashboard to follow results. Offline nodes are skipped."
    }
    private fun work(actor: PortalCommandActor, task: () -> String) {
        PortalApplication.runtime.executor.execute {
            try { actor.sendRawMessage(task()) } catch (failure: Exception) { actor.audience.sendFailure(failure.message ?: "Network request failed") }
        }
    }
}
