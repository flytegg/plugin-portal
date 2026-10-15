package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.chat.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.CommandPlaceholder
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Optional
import gg.flyte.pluginportal.common.commands.lamp.CommandPermission
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import revxrsal.commands.node.ExecutionContext

@Command("pp", "pluginportal", "ppm")
@CommandPermission("pluginportal.view")
class HelpSubCommand {

    @CommandPlaceholder
    @CommandPermission("pluginportal.view")
    fun rootCommand(actor: PortalCommandActor, context: ExecutionContext<PortalCommandActor>) {
        val path = context.input().source().substringAfter(' ', "").trim()
        val topic = path.substringBefore(' ')
        // A failed @RequiresAuth condition falls through to this placeholder; say why.
        if (topic.isNotEmpty() && !gg.flyte.pluginportal.plugin.PortalApplication.isAuthed() && context.lamp().registry().commands().any {
                it.annotations().contains(gg.flyte.pluginportal.plugin.commands.lamp.RequiresAuth::class.java) &&
                    it.path().split(' ').getOrNull(1).equals(topic, ignoreCase = true)
            }) return actor.audience.sendFailure(gg.flyte.pluginportal.plugin.PortalApplication.lockedPremiumMessage())
        if (topic.isEmpty()) helpCommand(actor)
        else if (!showCommandHelp(actor, topic)) {
            actor.audience.sendFailure("Unknown command. Use /pp help for available commands.")
        }
    }

    @Subcommand("help")
    @CommandPermission("pluginportal.view")
    fun helpCommand(
        actor: PortalCommandActor,
        @Optional @Named("topic") topic: String = "1"
    ) {
        val audience = actor.audience
        val pageNumber = topic.toIntOrNull()
        if (pageNumber == null) {
            if (!showCommandHelp(actor, topic)) audience.sendFailure("Unknown topic. Use /pp help or /pp help <command>.")
            return
        }
        if (pageNumber !in helpPages.indices.map { it + 1 }) {
            return audience.sendFailure("Choose help page 1 to ${helpPages.size}, or a command such as /pp help install.")
        }
        val page = helpPages[pageNumber - 1]
        var message = Component.empty().append(textPrimary("Plugin Portal").bold())
            .append(textDark("  /  ${page.first}"))
        page.second.filter { (command, _) -> actor.hasPermission(helpPermission(command.substringBefore(' '))) }.forEach { (command, description) ->
            message = message.appendNewline().append(helpLine(command, description))
        }
        audience.sendMessage(message.appendNewline().appendNewline().append(footer(pageNumber)).boxed())
    }

    fun showCommandHelp(actor: PortalCommandActor, topic: String): Boolean {
        val audience = actor.audience
        val command = when (topic.lowercase()) {
            "delete" -> "uninstall"
            "version" -> "info"
            "config" -> "reload"
            else -> topic.lowercase()
        }
        val help = commandHelp[command] ?: return false
        if (!actor.hasPermission(helpPermission(command))) {
            audience.sendFailure("You do not have permission to use this command.")
            return true
        }
        var message = Component.empty().append(textPrimary(help.first).bold())
        help.second.forEach { message = message.appendNewline().append(textSecondary(it)) }
        audience.sendMessage(message.boxed())
        return true
    }

    private fun helpPermission(command: String): String = when (command) {
        "refresh", "servers", "link", "unlink", "history" -> "pluginportal.network"
        "control", "dashboard" -> "pluginportal.admin"
        else -> "pluginportal.view"
    }

    private val helpPages = listOf(
        "Plugins" to listOf(
            "list" to "Installed plugins",
            "search <query>" to "Find a plugin",
            "view <plugin>" to "Plugin details",
            "install <plugin>" to "Install a plugin",
            "update <plugin>" to "Update a plugin",
            "blacklist [plugin]" to "Skip bulk updates",
            "uninstall <plugin>" to "Remove a plugin",
            "info" to "Version and connection"
        ),
        "Premium tools" to listOf(
            "editor" to "Open the browser editor",
            "updateAll" to "Update tracked plugins",
            "recognize <file>" to "Track a local JAR",
            "recognizeAll" to "Track unrecognized JARs",
            "external" to "GitHub and GeyserMC plugins",
            "import <url>" to "Import an exported list",
            "export" to "Share your plugin list",
            "scan <file>" to "Scan a local JAR"
        ),
        "Settings and network" to listOf(
            "servers" to "Connected servers",
            "link [code]" to "Link status or link this server",
            "unlink" to "Disconnect this server",
            "refresh --servers <names>" to "Refresh selected inventories",
            "history" to "Network operation results",
            "control" to "Remote plugin writes, console only",
            "reload" to "Reload configuration",
            "key" to "Manage your API key",
            "upgrade" to "Update Plugin Portal",
            "platform <plugin> <platform>" to "Change marketplace",
            "install-url <url>" to "Direct install, console only",
            "dump" to "Support diagnostics",
            "support" to "Get help"
        )
    )

    private val commandHelp = mapOf(
        "refresh" to ("/pp refresh --servers <name,name>" to listOf("Controller only. Refresh inventories on explicit servers by name or UUID.", "Use --server as an alias. Offline servers are skipped; no implicit all-server target.")),
        "servers" to ("/pp servers [--page <number>] [--full]" to listOf("List connected servers. Controller access is required.", "Use names or UUIDs with --server and --servers.")),
        "link" to ("/pp link [code|confirm|cancel]" to listOf("Without arguments, show account, network, connection, write approval and restart count.", "Get a link code from Add server in the dashboard.", "Link changes are console only. Review the account, network and role from your dashboard link command.", "Confirm with /pp link confirm or discard with /pp link cancel. Writes start disabled.")),
        "unlink" to ("/pp unlink" to listOf("Console only. Disconnect this server and remove its local credential.", "Revoke the server in your dashboard as well.")),
        "history" to ("/pp history [operationId] [--page <number>] [--full]" to listOf("Show recent operations, or results for one operation.")),
        "control" to ("/pp control <enable|disable|status>" to listOf("Console only. Remote plugin changes start disabled.", "Approve or disable dashboard and controller writes on this server.")),
        "dashboard" to ("/pp dashboard <enable|disable|status>" to listOf("Compatibility alias for /pp control.", "Approval covers dashboard and controller plugin changes.")),
        "help" to ("/pp help [page or command]" to listOf(
            "Choose page 1 to 3, or a command name.",
            "Example: /pp help platform"
        )),
        "install" to ("/pp install <name> [platform] [channel] [--byId] [--exact] [--version <version>]" to listOf(
            "Install a compatible plugin. Restart the server to load it.",
            "Quote names with spaces. --byId requires a platform and its project ID.",
            "The channel is positional: release, beta, alpha, or a provider channel.",
            "An exact version is excluded from updateAll.",
            "Add --server <name> or --servers <name,name> for explicit remote targets.",
            "Example: /pp install LuckPerms MODRINTH release"
        )),
        "update" to ("/pp update <name> [--byId] [--refresh] [--channel <name>] [--version <version>] [--ignoreOutdated]" to listOf(
            "Update one tracked plugin. Restart the server to apply the update.",
            "--refresh bypasses the local marketplace cache, not the API scanner.",
            "--ignoreOutdated reinstalls the selected compatible version.",
            "--version selects an exact version and excludes it from updateAll.",
            "Add --server <name> or --servers <name,name> to target remote servers.",
            "Example: /pp update LuckPerms --refresh"
        )),
        "updateall" to ("/pp updateAll [--ignoreOutdated]" to listOf(
            "update tracked marketplace plugins. Restart to apply updates.",
            "Excluded plugins are skipped. Use /pp blacklist to view exclusions.",
            "External plugins use /pp external updateAll."
        )),
        "list" to ("/pp list [--all] [--untracked] [--outdated] [--external] [--detailed] [--page <number>] [--full]" to listOf(
            "--untracked shows only unmanaged JARs. Use it without --all, --outdated, or --external.",
            "--all includes unrecognized JARs. It cannot be combined with --outdated or --external.",
            "--outdated checks for updates without installing them.",
            "--external shows only configured external plugins.",
            "Chat shows eight entries per page. Console shows all entries by default.",
            "Use --full or --page, not both.",
            "Add --server <name> or --servers <name,name> to view remote inventories.",
            "Pending changes show Restart to apply, with the staged version for updates."
        )),
        "search" to ("/pp search <query> [platform] [--page <number>] [--full]" to listOf(
            "Search the marketplace. Quote queries with spaces.",
            "Example: /pp search \"ViaVersion\" MODRINTH"
        )),
        "view" to ("/pp view <name> [platform] [--byId] [--exact]" to listOf(
            "View marketplace details. --byId requires a platform.",
            "Example: /pp view LuckPerms MODRINTH --exact"
        )),
        "blacklist" to ("/pp blacklist [name] [--byId]" to listOf(
            "Without a name, list plugins excluded from updateAll.",
            "With a name, toggle the exclusion. The plugin stays tracked.",
            "A deliberate /pp update still works."
        )),
        "platform" to ("/pp platform <name> <platform> [--byId]" to listOf(
            "Switch to another marketplace source for the same merged plugin.",
            "Platforms: MODRINTH, HANGAR, SPIGOTMC, POLYMART.",
            "Example: /pp platform LuckPerms MODRINTH",
            "Restart after the download."
        )),
        "uninstall" to ("/pp uninstall <name> [--byId] [--server <name>] [--servers <names>]" to listOf(
            "Delete the tracked JAR. Keep the plugin data folder.",
            "Restart the server to unload the plugin. Alias: /pp delete."
        )),
        "install-url" to ("/pp install-url <url>" to listOf(
            "Console only: download a JAR from a direct URL. Restart to load it.",
            "Use a trusted source. A raw URL does not establish a marketplace update source."
        )),
        "key" to ("/pp key <set <key> | get | clear>" to listOf(
            "Set a catalog or legacy purchase key. Keys never link a server or grant remote control.",
            "Do not share keys, screenshots of keys, or command logs that contain keys."
        )),
        "upgrade" to ("/pp upgrade [--channel <name>] [--yes]" to listOf(
            "Check for a Plugin Portal update. The default channel is release.",
            "--yes downloads the update. Restart the server to apply it."
        )),
        "recognize" to ("/pp recognize <file> [--channel <name>]" to listOf(
            "track a manually installed JAR that a marketplace recognizes.",
            "The file must be in plugins/. Recognition can rename it.",
            "--channel saves the channel for future updates."
        )),
        "recognizeall" to ("/pp recognizeAll [--channel <name>]" to listOf(
            "recognize untracked JARs in plugins/.",
            "Plugin Portal skips itself and managed external files.",
            "An unknown file remains untracked."
        )),
        "editor" to ("/pp editor [status | url | reconnect | stop]" to listOf(
            "open a temporary browser editor session.",
            "Treat the editor URL as a secret. /pp connect is not supported.",
            "Use status to check the connection or stop to end the session."
        )),
        "import" to ("/pp import <mclogs-url>" to listOf(
            "install plugins from a /pp export link.",
            "The export contains marketplace IDs, not exact versions or plugin configuration."
        )),
        "export" to ("/pp export" to listOf(
            "export tracked marketplace IDs to MCLogs.",
            "This is not a server backup. External plugins and configuration are not included."
        )),
        "scan" to ("/pp scan <file>" to listOf(
            "scan a local JAR with the bundled scanner.",
            "A scan result does not guarantee that a plugin is safe."
        )),
        "external" to ("/pp external <action>" to listOf(
            "manage GitHub Releases and GeyserMC plugins.",
            "Add: /pp external add github <id> <owner> <repo> <asset> [--prereleases]",
            "Add: /pp external add geysermc <id> <project> <artifact>",
            "Import: replace add with import and append <file> before flags.",
            "Actions: check, install, update, uninstall, invalidate <id>",
            "Other actions: updateAll, reload",
            "Configuration: plugins/PluginPortal/external-plugins.yml",
            "Uninstall keeps configuration. Restart after file changes."
        )),
        "reload" to ("/pp reload" to listOf(
            "Reload config.yml, plugins.json, and external-plugins.yml.",
            "Aliases: /pp config reload, /pp config refresh.",
            "This does not reload installed plugins or apply staged updates."
        )),
        "info" to ("/pp info" to listOf(
            "Show the exact version, platform, plan, network connection, and pending restart count.",
            "Alias: /pp version."
        )),
        "dump" to ("/pp dump" to listOf(
            "Upload sanitized diagnostic information for support.",
            "Review diagnostic files before sharing them publicly."
        )),
        "support" to ("/pp support" to listOf(
            "Show the support link."
        ))
    )

    private fun helpLine(command: String, description: String): Component {
        val topic = command.substringBefore(' ').lowercase()
        val help = commandHelp[topic]
        var hover: Component = textPrimary(help?.first ?: "/pp $command")
            .appendNewline().append(textSecondary(description))
        help?.second?.forEach { hover = hover.appendNewline().append(textSecondary(it)) }
        return textDark(" - ")
            .append(textPrimary("/pp $command")
                .clickEvent(ClickEvent.suggestCommand("/pp ${command.substringBefore(' ')}" + if (' ' in command) " " else ""))
                .hoverEvent(HoverEvent.showText(hover.appendNewline().append(textDark("Click to suggest command")))))
            .append(textSecondary("  $description"))
    }

    private fun footer(page: Int): Component {
        var footer: Component = textSecondary("Page $page/${helpPages.size}")
        if (page > 1) footer = footer.append(textPrimary("  [Previous]")
            .clickEvent(ClickEvent.runCommand("/pp help ${page - 1}")))
        if (page < helpPages.size) footer = footer.append(textPrimary("  [Next]")
            .clickEvent(ClickEvent.runCommand("/pp help ${page + 1}")))
        return footer.append(textPrimary("  [Docs]")
            .clickEvent(ClickEvent.openUrl("https://pluginportal.link/docs/reference/commands")))
            .appendNewline().append(textDark("Hover for details. /pp help <command>"))
    }
}
