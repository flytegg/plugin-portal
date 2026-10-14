package gg.flyte.pluginportal.plugin.network

import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.CommandPermission
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import gg.flyte.pluginportal.plugin.PortalApplication
import net.kyori.adventure.text.Component
import revxrsal.commands.annotation.*

@Command("pp", "pluginportal", "ppm")
class ServerCommands {
    @Subcommand("link")
    @CommandPermission("pluginportal.network")
    fun link(actor: PortalCommandActor, @Named("code") code: String) = NetworkCommands().enroll(actor, code)

    @Subcommand("unlink")
    @CommandPermission("pluginportal.network")
    fun unlink(actor: PortalCommandActor) = NetworkCommands().leave(actor)

    @Subcommand("servers")
    @CommandPermission("pluginportal.network")
    fun servers(actor: PortalCommandActor, @Optional @Flag("page") page: Int? = null, @Switch("full") full: Boolean = false) =
        NetworkCommands().showServers(actor, page, full, "/pp servers")

    @Subcommand("history")
    @CommandPermission("pluginportal.network")
    fun history(actor: PortalCommandActor, @Optional @Named("operationId") id: String? = null,
                @Optional @Flag("page") page: Int? = null, @Switch("full") full: Boolean = false) {
        if (id == null) NetworkCommands().showOperations(actor, page, full, "/pp history")
        else NetworkCommands().showOperation(actor, id, page, full, "/pp history $id")
    }

    @Subcommand("control", "dashboard")
    @CommandPermission("pluginportal.admin")
    fun dashboard(actor: PortalCommandActor, @Optional @Suggest("enable", "disable", "status") action: String = "status") {
        if (!actor.isConsole) return actor.audience.sendFailure("Run /pp control $action in this server's console.")
        when (action.lowercase()) {
            "enable", "disable" -> {
                Config.setRemoteWrites(action.equals("enable", true))
                PortalApplication.network.refreshInventory()
            }
            "status" -> Unit
            else -> return actor.audience.sendInfo("Use /pp control <enable|disable|status>. Only this server's console can approve remote plugin changes.")
        }
        actor.audience.sendMessage(Component.empty().append(textPrimary("Remote plugin control").bold())
            .appendNewline().append(textSecondary("Writes  ")).append(textPrimary(if (Config.allowsRemoteWrites()) "Approved" else "Disabled"))
            .appendNewline().append(textDark("Applies to dashboard and network controller plugin changes on this server."))
            .appendNewline().append(textSecondary("Enable  ")).append(textPrimary("/pp control enable"))
            .appendNewline().append(textSecondary("Disable  ")).append(textPrimary("/pp control disable"))
            .appendNewline().append(textDark("Local commands stay available. Restart to apply JAR changes."))
            .boxed())
    }
}
