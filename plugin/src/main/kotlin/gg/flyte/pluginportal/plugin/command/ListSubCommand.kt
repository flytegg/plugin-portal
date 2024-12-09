package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.chat.*
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.manager.MarketplacePluginCache.isUpToDate
import gg.flyte.pluginportal.plugin.util.SharedComponents
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class ListSubCommand {

    private val BUTTON_PIXEL_LENGTH = "[Update] [Uninstall]".pixelLength()
    private val UPTODATE_BUTTON_PIXEL_LENGTH = "[Up to date] [Uninstall]".pixelLength()

    @Subcommand("list",)
    @CommandPermission("pluginportal.view")
    fun listCommand(audience: Audience) {
        val plugins = LocalPluginCache
            .sortedBy { plugin -> plugin.name }
            .filter { plugin -> plugin.name != PluginPortal.instance.name }

        if (plugins.isEmpty()) return audience.sendMessage(text("No plugins found", NamedTextColor.GRAY).boxed())

        var message = text("Plugins installed with Plugin Portal", NamedTextColor.GRAY)

        plugins.forEach { plugin ->
            val buttonLength = if (plugin.isUpToDate) UPTODATE_BUTTON_PIXEL_LENGTH else BUTTON_PIXEL_LENGTH
            val name = plugin.name.shortenToLine(plugin.platform.toString().pixelLength() + 12 + buttonLength)
            message = message.append(text("\n"))
                .append(text(" - ", NamedTextColor.DARK_GRAY))
                .append(textPrimary(name)
                    .hoverEvent(HoverEvent.showText(
                        text("Click to view ", NamedTextColor.GRAY).appendPrimary(plugin.name)))
                    .suggestCommand("/pp view \"${plugin.platformId}\" ${plugin.platform} --byId")
                )
                .append(textDark(" (${plugin.platform.name}) "))
                .append(SharedComponents.getUpdateButton(plugin))
                .append(text(" "))
                .append(SharedComponents.getInstallButton(plugin, true))
        }

        audience.sendMessage(message.boxed())
    }

}