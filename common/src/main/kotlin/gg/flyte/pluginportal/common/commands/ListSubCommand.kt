package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.EnabledCommand
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.util.SharedComponents
import gg.flyte.pluginportal.common.util.async
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.Switch
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class ListSubCommand {

    private val BUTTON_PIXEL_LENGTH = "[Update] [Uninstall]".pixelLength()
    private val UPTODATE_BUTTON_PIXEL_LENGTH = "[Up to date] [Uninstall]".pixelLength()

    @EnabledCommand(Features.LIST)
    @Subcommand("list")
    @CommandPermission("pluginportal.view")
    fun listCommand(
        audience: Audience,
        @Switch("detailed") detailed: Boolean = false,
    ) {
        async {
            val plugins = LocalPluginCache
                .sortedBy { plugin -> plugin.name }
                .filter { plugin -> plugin.name != PluginPortalBase.plugin.name }

            if (plugins.isEmpty()) return@async audience.sendMessage(
                Component.text("No plugins found", NamedTextColor.GRAY).boxed())

            var message = Component.text("Plugins installed with Plugin Portal", NamedTextColor.GRAY)
            val console = audience.isConsole()
            val showDetails = detailed || console

            plugins.forEach { plugin ->
                message = message.append(Component.text("\n"))
                    .append(if (showDetails) getDetailedPluginLine(plugin) else getCompactPluginLine(plugin, console))
            }

            audience.sendMessage(message.boxed())
        }
    }

    private fun getCompactPluginLine(plugin: LocalPlugin, console: Boolean): Component {
        val buttonLength = if (plugin.isUpToDate) UPTODATE_BUTTON_PIXEL_LENGTH else BUTTON_PIXEL_LENGTH
        val name = plugin.name.shortenToLine(plugin.platform.toString().pixelLength() + 12 + buttonLength)

        var line = Component.text(" - ", NamedTextColor.DARK_GRAY)
            .append(
                textPrimary(name)
                    .hoverEvent(
                        HoverEvent.showText(
                            Component.text("Click to view ", NamedTextColor.GRAY).appendPrimary(plugin.name)))
                    .suggestCommand("/pp view \"${plugin.platformId}\" ${plugin.platform} --byId")
            )
            .append(textDark(" (${plugin.platform.name}) "))
            .append(SharedComponents.getUpdateButton(plugin))

        if (!console) line = line
            .append(Component.text(" "))
            .append(SharedComponents.getInstallButton(plugin, true))

        return line
    }

    private fun getDetailedPluginLine(plugin: LocalPlugin): Component {
        val marketplace = MarketplacePluginCache.getCachedPluginById(plugin.platform, plugin.platformId)
        val targetVersion = marketplace?.let(plugin::targetUpdateVersion)?.versionNumber
        val status = if (plugin.isUpToDate) "Up to date" else "Update available"

        var line = Component.text(" - ", NamedTextColor.DARK_GRAY)
            .append(
                textPrimary(plugin.name)
                    .hoverEvent(
                        HoverEvent.showText(
                            Component.text("Click to view ", NamedTextColor.GRAY).appendPrimary(plugin.name)))
                    .suggestCommand("/pp view \"${plugin.platformId}\" ${plugin.platform} --byId")
            )
            .append(textDark(" (${plugin.platform.name}) "))
            .append(textSecondary("id="))
            .append(textPrimary(plugin.platformId))
            .append(textDark(" version="))
            .append(Component.text(plugin.version, if (plugin.isUpToDate) NamedTextColor.GREEN else NamedTextColor.RED))

        if (targetVersion != null && targetVersion != plugin.version) {
            line = line
                .append(textDark(" latest="))
                .append(Component.text(targetVersion, NamedTextColor.GREEN))
        }

        line = line
            .append(textDark(" status="))
            .append(Component.text(status, if (plugin.isUpToDate) NamedTextColor.GRAY else NamedTextColor.AQUA))

        if (!plugin.isUpToDate) {
            line = line
                .append(textDark(" command="))
                .append(textPrimary("/pp update \"${plugin.platformId}\" --byId"))
        }

        return line
    }

}
