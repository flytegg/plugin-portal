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
    fun link(actor: PortalCommandActor, @Optional @Named("code") code: String? = null) {
        if (code == null) NetworkMessages().showLinkStatus(actor)
        else NetworkMessages().reviewLink(actor, code)
    }

    @Subcommand("unlink")
    @CommandPermission("pluginportal.network")
    fun unlink(actor: PortalCommandActor) = NetworkMessages().unlink(actor)

    @Subcommand("servers")
    @CommandPermission("pluginportal.network")
    fun servers(actor: PortalCommandActor, @Optional @Flag("page") page: Int? = null, @Switch("full") full: Boolean = false) =
        NetworkMessages().showServers(actor, page, full, "/pp servers")

    @Subcommand("history")
    @CommandPermission("pluginportal.network")
    fun history(actor: PortalCommandActor, @Optional @Named("operationId") id: String? = null,
                @Optional @Flag("page") page: Int? = null, @Switch("full") full: Boolean = false) {
        if (id == null) NetworkMessages().showOperations(actor, page, full, "/pp history")
        else NetworkMessages().showOperation(actor, id, page, full, "/pp history $id")
    }

    @Subcommand("refresh")
    @CommandPermission("pluginportal.network")
    fun refresh(actor: PortalCommandActor,
                @Optional @Flag("server") @SuggestWith(ServerTargetSuggestionProvider::class) server: String? = null,
                @Optional @Flag(value = "servers", shorthand = 'S') @SuggestWith(ServerTargetSuggestionProvider::class) servers: String? = null) {
        if (!ServerTargets.requested(actor, server, servers) { ServerTargets.refresh(actor, it) }) {
            actor.audience.sendFailure("Choose explicit targets: /pp refresh --servers <name,name>. Use /pp servers to list them.")
        }
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
