package gg.flyte.pluginportal.plugin.network

import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.CommandPermission
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import gg.flyte.pluginportal.plugin.PortalApplication
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

    @Subcommand("dashboard")
    @CommandPermission("pluginportal.admin")
    fun dashboard(actor: PortalCommandActor, @Optional @Suggest("enable", "disable", "status") action: String = "status") {
        if (!actor.isConsole) return actor.audience.sendFailure("Run /pp dashboard $action in this server's console.")
        when (action.lowercase()) {
            "enable", "disable" -> {
                Config.setDashboardWrites(action.equals("enable", true))
                PortalApplication.network.refreshInventory()
            }
            "status" -> Unit
            else -> return actor.audience.sendInfo("Use /pp dashboard <enable|disable|status>. Only this server's console can change dashboard access.")
        }
        actor.audience.sendInfo(if (Config.allowsDashboardWrites()) "Dashboard control enabled for this server. Use /pp dashboard disable to return to read-only."
            else "Dashboard is read-only for this server. Use /pp dashboard enable to allow dashboard plugin changes.")
    }
}
