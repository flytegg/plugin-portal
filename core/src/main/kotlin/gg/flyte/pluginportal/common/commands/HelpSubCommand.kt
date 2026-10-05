package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.chat.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.CommandPlaceholder
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Optional
import gg.flyte.pluginportal.common.commands.lamp.CommandPermission

@Command("pp", "pluginportal", "ppm")
@CommandPermission("pluginportal.view")
class HelpSubCommand {

    @CommandPlaceholder
    @CommandPermission("pluginportal.view")
    fun rootCommand(audience: Audience) = helpCommand(audience)

    @Subcommand("help")
    @CommandPermission("pluginportal.view")
    fun helpCommand(
        audience: Audience,
        @Optional @Named("topic") topic: String = "1"
    ) {
        val GOLD = TextColor.color(0xfebe00)
        val pageNumber = topic.toIntOrNull()
        if (pageNumber == null) {
            val command = when (topic.lowercase()) {
                "delete" -> "uninstall"
                "version" -> "info"
                "config" -> "reload"
                else -> topic.lowercase()
            }
            val help = commandHelp[command]
                ?: return audience.sendFailure("Unknown help topic. Use /pp help or /pp help 2.")
            var message = textPrimary(help.first).bold().appendNewline()
            help.second.forEach { message = message.appendNewline().append(textSecondary(it)) }
            audience.sendMessage(message.boxed())
            return
        }
        if (pageNumber !in 1..2) return audience.sendFailure("Choose help page 1 or 2, or a command such as /pp help install")

        var message = centerComponentLine(
            textPrimary("Plugin Portal").bold()
        ).appendNewline().append(
            centerComponentLine(
                textSecondary("by ").append(
                    text("Flyte", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                        .showOnHover("Click here to join our Discord", NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.openUrl("https://flyte.gg/discord"))
                )
            )
        ).appendNewline().appendNewline()

        message = if (pageNumber == 1) {
            message.append(section("Plugins"))
                .appendNewline()
                .append(helpLine("/pp list [--outdated] [--external] [--page <number>]", "Installed", details = listOf(
                    "--outdated shows marketplace and external updates.",
                    "--external shows only configured external plugins.",
                    "--detailed includes versions, IDs, and sources.",
                    "--full shows every entry. Console output is complete by default."
                )))
                .appendNewline()
                .append(helpLine("/pp search <query> [platform]", "Search marketplace"))
                .appendNewline()
                .append(helpLine("/pp view <plugin> [platform]", "Details", details = listOf(
                    "Flags: --byId, --exact or -e",
                    "Example: /pp view luckperms modrinth --exact"
                )))
                .appendNewline()
                .append(helpLine("/pp install <plugin> [platform] [channel]", "Install", details = listOf(
                    "Flags: --byId, --exact or -e, --version <version>",
                    "Command-selected versions are skipped by updateAll.",
                    "Example: /pp install luckperms modrinth --version 5.4.134"
                )))
                .appendNewline()
                .append(helpLine("/pp update <plugin>", "Update", details = listOf(
                    "Flags: --byId, --ignoreOutdated, --refresh, --channel <name>, --version <version>",
                    "--refresh bypasses Plugin Portal's local marketplace cache.",
                    "--channel disambiguates duplicate version names.",
                    "Example: /pp update luckperms --version 5.4.134"
                )))
                .appendNewline()
                .append(helpLine("/pp blacklist [plugin]", "Skip updateAll", details = listOf(
                    "No plugin lists blacklisted plugins.",
                    "Example: /pp blacklist essentialsx"
                )))
                .appendNewline()
                .append(helpLine("/pp platform <plugin> <platform>", "Switch platform", details = listOf(
                    "Downloads from the new platform when available.",
                    "Example: /pp platform essentialsx modrinth"
                )))
                .appendNewline()
                .append(helpLine("/pp <uninstall | delete> <plugin>", "Remove", details = listOf(
                    "Flags: --byId",
                    "Example: /pp uninstall luckperms"
                )))
                .appendNewline()
                .append(helpLine("/pp <version | info>", "Status"))
                .appendNewline()
                .appendNewline()
                .append(helpLine("/pp help <command>", "Command details"))
                .appendNewline()
                .append(footer(2))
        } else {
            message.append(section("More Commands"))
                .appendNewline()
                .append(helpLine("/pp install-url <url>", "Direct install", details = listOf(
                    "Example: /pp install-url https://example.com/plugin.jar"
                )))
                .appendNewline()
                .append(helpLine("/pp key <set | get | clear>", "API key", details = listOf(
                    "Example: /pp key set pp_live_..."
                )))
                .appendNewline()
                .append(helpLine("/pp upgrade [--channel <name>]", "Self-update", details = listOf(
                    "Flag: --yes skips the confirmation prompt.",
                    "Example: /pp upgrade --yes"
                )))
                .append(localToolsHelp(GOLD))
                .appendNewline()
                .appendNewline()
                .append(footer(1))
        }

        audience.sendMessage(
            message.boxed()
        )
    }

    private val commandHelp = mapOf(
        "install" to ("/pp install <name> [platform] [channel] [--byId] [--exact] [--version <version>]" to listOf(
            "Install a compatible plugin. Restart the server to load it.",
            "Quote names with spaces. --byId requires a platform and its project ID.",
            "The channel is positional: release, beta, alpha, or a provider channel.",
            "An exact version is excluded from updateAll.",
            "Example: /pp install LuckPerms MODRINTH release"
        )),
        "update" to ("/pp update <name> [--byId] [--refresh] [--channel <name>] [--version <version>] [--ignoreOutdated]" to listOf(
            "Update one tracked plugin. Restart the server to apply the update.",
            "--refresh bypasses the local marketplace cache, not the API scanner.",
            "--ignoreOutdated reinstalls the selected compatible version.",
            "--version selects an exact version and excludes it from updateAll.",
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
            "Use --full or --page, not both."
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
            "Restart after the download."
        )),
        "uninstall" to ("/pp uninstall <name> [--byId]" to listOf(
            "Delete the tracked JAR. Keep the plugin data folder.",
            "Restart the server to unload the plugin. Alias: /pp delete."
        )),
        "install-url" to ("/pp install-url <url>" to listOf(
            "Console only: download a JAR from a direct URL. Restart to load it.",
            "Use a trusted source. A raw URL does not establish a marketplace update source."
        )),
        "key" to ("/pp key <set <key> | get | clear>" to listOf(
            "Set and validate a key for paid network management.",
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
            "Show Plugin Portal version, license state, and update information.",
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

    private fun section(name: String, color: TextColor = NamedTextColor.AQUA): Component =
        text(name, color, TextDecoration.BOLD)

    private fun helpLine(
        command: String,
        description: String,
        color: TextColor = NamedTextColor.AQUA,
        details: List<String> = emptyList()
    ): Component =
        textSecondary(" - ")
            .append(
                text(command, color)
                    .clickEvent(ClickEvent.suggestCommand(command))
                    .hoverEvent(HoverEvent.showText(helpHover(command, description, details)))
            )
            .append(textSecondary(" - $description"))

    private fun localToolsHelp(gold: TextColor): Component =
        Component.empty()
            .appendNewline()
            .appendNewline()
            .append(section("Local tools", gold))
            .appendNewline()
            .append(helpLine("/pp editor [status | url | reconnect | stop]", "Editor", gold, listOf(
                "Creates a temporary browser editor session.",
                "Example: /pp editor url"
            )))
            .appendNewline()
            .append(helpLine("/pp updateAll", "Bulk update", gold, listOf(
                "Flag: --ignoreOutdated reinstalls even when versions match.",
                "Example: /pp updateAll --ignoreOutdated"
            )))
            .appendNewline()
            .append(helpLine("/pp recognize <file> [--channel <name>]", "Recognize", gold, listOf(
                "Example: /pp recognize SomePlugin.jar --channel release"
            )))
            .appendNewline()
            .append(helpLine("/pp recognizeAll [--channel <name>]", "Recognize all", gold))
            .appendNewline()
            .append(helpLine("/pp <import | export>", "Import/export", gold, listOf(
                "Import requires an MCLogs URL created by /pp export.",
                "Example: /pp import https://mclo.gs/abc123"
            )))
            .appendNewline()
            .append(helpLine("/pp scan <plugin>", "Scan", gold, listOf(
                "Example: /pp scan SomePlugin.jar"
            )))

    private fun helpHover(command: String, description: String, details: List<String>): Component {
        var hover = text(command, NamedTextColor.AQUA)
            .appendNewline()
            .append(text(description, NamedTextColor.GRAY))

        details.forEach { detail ->
            hover = hover.appendNewline().append(text(detail, NamedTextColor.GRAY))
        }

        return hover.appendNewline()
            .append(text("Click to suggest command", NamedTextColor.DARK_GRAY))
    }

    private fun footer(page: Int): Component =
        textSecondary("Docs: ").append(
            text("pluginportal.link/docs/commands", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl("https://pluginportal.link/docs/commands"))
                .hoverEvent(HoverEvent.showText(text("Open Plugin Portal command docs", NamedTextColor.AQUA)))
        ).append(textSecondary("  |  ")).append(
            text(if (page == 2) "/pp help 2" else "/pp help", NamedTextColor.AQUA)
                .clickEvent(ClickEvent.suggestCommand(if (page == 2) "/pp help 2" else "/pp help"))
                .hoverEvent(HoverEvent.showText(text(if (page == 2) "Show page 2" else "Back to page 1", NamedTextColor.AQUA)))
        )
}
