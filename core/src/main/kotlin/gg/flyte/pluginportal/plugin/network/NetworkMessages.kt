package gg.flyte.pluginportal.plugin.network

import com.google.gson.JsonObject
import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import gg.flyte.pluginportal.plugin.PortalApplication
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor.*

/** Shared presentation for the short server commands; this class registers no command paths. */
internal class NetworkMessages {
    private val client get() = PortalApplication.network

    fun reviewLink(actor: PortalCommandActor, code: String) {
        if (!actor.isConsole) return actor.audience.sendFailure("Run enrollment in the server or proxy console.")
        work(actor) {
            when (code.lowercase()) {
                "confirm" -> {
                    client.confirmEnrollment()
                    actor.audience.sendSuccess("Server linked. Remote plugin changes remain disabled; approve them with /pp control enable in this console.")
                }
                "cancel" -> {
                    client.cancelEnrollment()
                    actor.audience.sendInfo("Pending link discarded. This server was not enrolled.")
                }
                else -> {
                    val review = client.reviewEnrollment(code)
                    actor.audience.sendMessage(Component.empty()
                        .append(textPrimary("Review server link").bold())
                        .appendNewline().append(textSecondary("Account  ")).append(textPrimary(review.ownerName))
                        .appendNewline().append(textDark("Account ID  ${review.ownerId}"))
                        .appendNewline().append(textSecondary("Network  ")).append(textPrimary(review.networkName))
                        .appendNewline().append(textDark("Network ID  ${review.networkId}"))
                        .appendNewline().append(textSecondary("Server  ")).append(textPrimary(review.name))
                        .appendNewline().append(textSecondary("Role  ")).append(textPrimary(review.role))
                        .appendNewline().append(textDark(if (review.role == "controller")
                            "This controller may request plugin changes on approved servers in this network."
                            else "This server will report its status and inventory to the account above."))
                        .appendNewline().append(textDark("Remote plugin writes start disabled. Confirm only if you trust this account."))
                        .appendNewline().append(textPrimary("/pp link confirm")).append(textSecondary("  or  ")).append(textPrimary("/pp link cancel"))
                        .boxed())
                }
            }
        }
    }

    fun unlink(actor: PortalCommandActor) {
        if (!actor.isConsole) return actor.audience.sendFailure("Run /pp unlink in the console.")
        work(actor) { client.leave(); actor.audience.sendSuccess(client.status) }
    }

    fun showLinkStatus(actor: PortalCommandActor) {
        val id = client.nodeId ?: return actor.audience.sendInfo("Not linked. In the dashboard, choose Add server to get a code, then run /pp link <code> in this console.")
        work(actor) {
            var message: Component = Component.empty().append(textPrimary("Server link").bold())
            client.ownerName?.let { message = message.appendNewline().append(textSecondary("Account  ")).append(textPrimary(it)) }
            message = message.appendNewline().append(textSecondary("Network  ")).append(textPrimary(client.networkName ?: client.networkId ?: "Unknown"))
                .appendNewline().append(textSecondary("Node  ")).append(textPrimary(id.take(8))).append(textSecondary("  ")).append(identifier(id, actor))
                .appendNewline().append(textSecondary("Role  ")).append(textPrimary(when (client.role) {
                    "controller" -> "Proxy · can send changes to servers that allow them"
                    "node" -> "Server"
                    else -> "Unknown"
                }))
                .appendNewline().append(textSecondary("Connection  ")).append(textPrimary(client.connectionState))
            client.lastError?.let { message = message.appendNewline().append(textSecondary("Last error  ")).append(textPrimary(it)) }
            val count = PortalApplication.runtime.inventory.snapshot().restartCount
            val allowed = Config.allowsRemoteWrites()
            message = message.appendNewline().append(textSecondary("Plugin changes  "))
                .append(textPrimary(if (allowed) "Allowed" else "Off"))
            if (!allowed) message = message.appendNewline().append(textDark("Allow dashboard and proxy changes: /pp control enable"))
            if (count > 0) message = message.appendNewline().append(textSecondary("Restart  "))
                .append(textPrimary("$count ${if (count == 1) "change applies" else "changes apply"} after restart"))
            actor.audience.sendMessage(message.boxed())
        }
    }

    fun showServers(actor: PortalCommandActor, page: Int?, full: Boolean, commandPath: String) = work(actor) {
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
        sendPagedRows(actor.audience, rows, page, full, commandPath)
    }

    fun showOperations(actor: PortalCommandActor, page: Int?, full: Boolean, commandPath: String) = work(actor) {
        if (!validPage(actor, page, full)) return@work
        val operations = client.state().getAsJsonArray("operations").map { it.asJsonObject }
        if (operations.isEmpty()) return@work actor.audience.sendInfo("No recent operations.")
        val rows = operations.map { operation -> "Recent operations" to {
            val id = operation.get("id").asString
            operationSummary(operation)
                .append(textSecondary("  ")).append(identifier(id, actor))
                .append(textPrimary("  [Results]")
                    .clickEvent(ClickEvent.runCommand("/pp history $id"))
                    .hoverEvent(HoverEvent.showText(textSecondary("Show outcomes for each target"))))
        } }
        sendPagedRows(actor.audience, rows, page, full, commandPath)
    }

    fun showOperation(actor: PortalCommandActor, operationId: String, page: Int?, full: Boolean, commandPath: String) = work(actor) {
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
        sendPagedRows(actor.audience, rows, page, full, commandPath)
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
