package gg.flyte.pluginportal.plugin.network

import com.google.gson.JsonObject
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.CommandPermission
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import gg.flyte.pluginportal.plugin.PortalApplication
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor.*
import revxrsal.commands.annotation.*

@Command("pp network", "ppnetwork")
@CommandPermission("pluginportal.network")
class NetworkCommands {
    private val client get() = PortalApplication.network

    @CommandPlaceholder
    fun root(actor: PortalCommandActor) = help(actor)

    @Subcommand("help")
    fun help(actor: PortalCommandActor) {
        var message: Component = Component.empty().append(textPrimary("Network management").bold())
            .append(textDark("  /  Paid"))
        listOf("status" to "This node's connection", "list" to "Nodes and online state",
            "operations" to "Recent results", "refresh" to "Refresh inventories",
            "install" to "Install a catalog plugin", "update" to "Update a catalog plugin",
            "uninstall" to "Remove a managed plugin").forEach { (command, description) ->
            val usage = when (command) {
                "refresh" -> "<nodeIds>"
                "install", "update" -> "<nodeIds> <platform> <pluginId> [version]"
                "uninstall" -> "<nodeIds> <platform> <pluginId>"
                else -> ""
            }
            message = message.appendNewline().append(textPrimary("/pp network $command")
                .clickEvent(ClickEvent.suggestCommand("/pp network $command" + if (usage.isEmpty()) "" else " "))
                .hoverEvent(HoverEvent.showText(textPrimary("/pp network $command $usage".trim())
                    .appendNewline().append(textSecondary("Use comma-separated node UUIDs.")))))
                .append(textSecondary("  $description"))
        }
        message = message.appendNewline().appendNewline().append(textDark("Restart each node to apply JAR changes."))
        if (actor.isConsole) message = message.appendNewline().append(textSecondary("Enrollment: /pp network enroll <code> | leave"))
        actor.audience.sendMessage(message.boxed())
    }

    @Subcommand("enroll")
    fun enroll(actor: PortalCommandActor, @Named("code") code: String) {
        if (!actor.isConsole) return actor.audience.sendFailure("Run enrollment in the server or proxy console.")
        work(actor) { client.enroll(code); actor.audience.sendSuccess("Node enrolled. ${client.status}.") }
    }

    @Subcommand("leave")
    fun leave(actor: PortalCommandActor) {
        if (!actor.isConsole) return actor.audience.sendFailure("Run network leave in the console.")
        work(actor) { client.leave(); actor.audience.sendSuccess(client.status) }
    }

    @Subcommand("status")
    fun status(actor: PortalCommandActor) {
        var message: Component = Component.empty().append(textPrimary("Network connection").bold())
            .appendNewline().append(textSecondary("Status  ")).append(textPrimary(client.status))
            .appendNewline().append(textSecondary("Role  ")).append(textPrimary(client.role ?: "Not enrolled"))
        client.nodeId?.let { message = message.appendNewline().append(textSecondary("Node  ")).append(identifier(it, actor)) }
        actor.audience.sendMessage(message.boxed())
    }

    @Subcommand("list")
    fun list(actor: PortalCommandActor, @Optional @Flag("page") page: Int? = null, @Switch("full") full: Boolean = false) = work(actor) {
        if (!validPage(actor, page, full)) return@work
        val nodes = client.state().getAsJsonArray("nodes").map { it.asJsonObject }
        if (nodes.isEmpty()) return@work actor.audience.sendInfo("No enrolled nodes.")
        val rows = nodes.map { node -> "Network nodes" to {
            val id = node.get("id").asString
            val revoked = node.get("revoked").asBoolean
            val online = node.get("online").asBoolean
            val state = if (revoked) "Revoked" else if (online) "Online" else "Offline"
            val color = if (revoked) RED else if (online) GREEN else GRAY
            var row: Component = textPrimary(node.get("name").asString.take(28))
                .append(textDark("  ${node.get("platform").asString}"))
                .append(Component.text("  $state", color))
                .append(textSecondary("  ")).append(identifier(id, actor))
            if (actor.isConsole) row = row.appendNewline().append(textDark("  Role: ${node.get("role").asString}"))
            row
        } }
        sendPagedRows(actor.audience, rows, page, full, "/pp network list")
    }

    @Subcommand("operations")
    fun operations(actor: PortalCommandActor, @Optional @Flag("page") page: Int? = null, @Switch("full") full: Boolean = false) = work(actor) {
        if (!validPage(actor, page, full)) return@work
        val operations = client.state().getAsJsonArray("operations").map { it.asJsonObject }
        if (operations.isEmpty()) return@work actor.audience.sendInfo("No recent operations.")
        val rows = operations.map { operation -> "Recent operations" to {
            val id = operation.get("id").asString
            operationSummary(operation)
                .append(textSecondary("  ")).append(identifier(id, actor))
                .append(textPrimary("  [Results]")
                    .clickEvent(ClickEvent.runCommand("/pp network operation $id"))
                    .hoverEvent(HoverEvent.showText(textSecondary("Show outcomes for each target"))))
        } }
        sendPagedRows(actor.audience, rows, page, full, "/pp network operations")
    }

    @Subcommand("operation")
    fun operation(actor: PortalCommandActor, @Named("operationId") operationId: String,
                  @Optional @Flag("page") page: Int? = null, @Switch("full") full: Boolean = false) = work(actor) {
        if (!validPage(actor, page, full)) return@work
        val state = client.state()
        val operation = state.getAsJsonArray("operations").map { it.asJsonObject }
            .find { it.get("id").asString == operationId }
            ?: return@work actor.audience.sendFailure("Operation not found in recent history.")
        val names = state.getAsJsonArray("nodes").associate { it.asJsonObject.let { node -> node.get("id").asString to node.get("name").asString } }
        val rows = operation.getAsJsonObject("results").entrySet().map { (id, value) ->
            "Operation ${operationId.take(8)}" to {
                val result = value.asJsonObject
                val status = result.get("status").asString
                var row: Component = textPrimary((names[id] ?: id.take(8)).take(28))
                    .append(Component.text("  $status", outcomeColor(status)))
                    .append(textSecondary("  ")).append(identifier(id, actor))
                val details = result.get("message")?.asString
                if (details != null) row = if (actor.isConsole) row.appendNewline().append(textSecondary("  $details"))
                    else row.hoverEvent(HoverEvent.showText(textSecondary(details)))
                row
            }
        }
        sendPagedRows(actor.audience, rows, page, full, "/pp network operation $operationId")
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

    private fun operate(actor: PortalCommandActor, targetIds: String, action: JsonObject) = work(actor) {
        val operation = client.submit(targetIds.split(','), action)
        val id = operation.get("id").asString
        actor.audience.sendMessage(Component.empty().append(textPrimary("Operation submitted").bold())
            .appendNewline().append(operationSummary(operation))
            .appendNewline().append(identifier(id, actor)).append(textPrimary("  [Results]")
                .clickEvent(ClickEvent.runCommand("/pp network operation $id")))
            .appendNewline().append(textDark("Offline targets are skipped." )).boxed())
    }

    private fun operationSummary(operation: JsonObject): Component {
        val action = operation.getAsJsonObject("action")
        var row: Component = textPrimary(action.get("kind").asString)
        action.get("id")?.asString?.let { row = row.append(textSecondary(" ${it.take(24)}")) }
        val counts = operation.getAsJsonObject("results").entrySet().groupingBy { it.value.asJsonObject.get("status").asString }.eachCount()
        counts.forEach { (status, count) -> row = row.append(Component.text("  $count $status", outcomeColor(status))) }
        return row
    }

    private fun outcomeColor(status: String) = when (status) {
        "succeeded" -> GREEN
        "failed", "unknown" -> RED
        "staged", "executing", "pending" -> YELLOW
        else -> GRAY
    }

    private fun identifier(id: String, actor: PortalCommandActor): Component =
        textDark(if (actor.isConsole) id else "[Copy ID]")
            .clickEvent(ClickEvent.copyToClipboard(id))
            .hoverEvent(HoverEvent.showText(textSecondary("$id\nClick to copy the full ID")))

    private fun validPage(actor: PortalCommandActor, page: Int?, full: Boolean): Boolean {
        if (page != null && page < 1) { actor.audience.sendFailure("Page must be at least 1."); return false }
        if (full && page != null) { actor.audience.sendFailure("Use --full or --page, not both."); return false }
        return true
    }

    private fun work(actor: PortalCommandActor, task: () -> Unit) {
        PortalApplication.runtime.executor.execute {
            try { task() } catch (failure: Exception) { actor.audience.sendFailure(failure.message ?: "Network request failed") }
        }
    }
}
