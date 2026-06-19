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
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.annotation.CommandPermission

// /pluginpportal help was broken so I removed it
@Command("pp", "pluginportal", "ppm", "pp help", "ppm help")
@CommandPermission("pluginportal.view")
class HelpSubCommand {

//    @Subcommand("help")
    @CommandPlaceholder
    @CommandPermission("pluginportal.view")
    fun helpCommand(
        audience: Audience,
        @Optional @Named("page") page: Int = 1
    ) {
        val GOLD = TextColor.color(0xfebe00)
        val pageNumber = page.coerceIn(1, 2)

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
                .append(helpLine("/pp list", "Installed"))
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
                    "Flags: --byId, --ignoreOutdated, --channel <name>, --version <version>",
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
                .append(helpLine("/pp <uninstall|delete> <plugin>", "Remove", details = listOf(
                    "Flags: --byId",
                    "Example: /pp uninstall luckperms"
                )))
                .appendNewline()
                .append(helpLine("/pp <version|info>", "Status"))
                .appendNewline()
                .appendNewline()
                .append(footer(2))
        } else {
            message.append(section("More Commands"))
                .appendNewline()
                .append(helpLine("/pp install-url <url>", "Direct install", details = listOf(
                    "Example: /pp install-url https://example.com/plugin.jar"
                )))
                .appendNewline()
                .append(helpLine("/pp key <set|get|clear>", "API key", details = listOf(
                    "Example: /pp key set pp_live_..."
                )))
                .appendNewline()
                .append(helpLine("/pp upgrade", "Self-update", details = listOf(
                    "Flag: --yes skips the confirmation prompt.",
                    "Example: /pp upgrade --yes"
                )))
                .append(premiumHelp(GOLD))
                .appendNewline()
                .appendNewline()
                .append(footer(1))
        }

        audience.sendMessage(
            message.boxed()
        )
    }

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

    private fun premiumHelp(gold: TextColor): Component =
        Component.text()
            .appendNewline()
            .appendNewline()
            .append(section("Premium", gold))
            .appendNewline()
            .append(helpLine("/pp editor [status|url|reconnect|stop]", "Editor", gold, listOf(
                "Creates a temporary browser editor session.",
                "Example: /pp editor url"
            )))
            .appendNewline()
            .append(helpLine("/pp updateAll", "Bulk update", gold, listOf(
                "Flag: --ignoreOutdated reinstalls even when versions match.",
                "Example: /pp updateAll --ignoreOutdated"
            )))
            .appendNewline()
            .append(helpLine("/pp recognize <file>", "Recognize", gold, listOf(
                "Example: /pp recognize SomePlugin.jar"
            )))
            .appendNewline()
            .append(helpLine("/pp recognizeAll", "Recognize all", gold))
            .appendNewline()
            .append(helpLine("/pp <import|export>", "Import/export", gold, listOf(
                "Import requires an MCLogs URL created by /pp export.",
                "Example: /pp import https://mclo.gs/abc123"
            )))
            .appendNewline()
            .append(helpLine("/pp scan <plugin>", "Scan", gold, listOf(
                "Example: /pp scan SomePlugin.jar"
            )))
            .build()

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
