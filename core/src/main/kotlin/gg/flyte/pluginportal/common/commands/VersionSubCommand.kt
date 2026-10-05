package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.CommandPermission
import gg.flyte.pluginportal.plugin.PortalApplication
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand

@Command("pp", "pluginportal", "ppm")
@CommandPermission("pluginportal.view")
class VersionSubCommand {
    @Subcommand("info", "version")
    @CommandPermission("pluginportal.view")
    fun onCommand(audience: Audience) {
        val plugin = PluginPortalBase.plugin
        val version = plugin.description.version
        val networkStatus = PortalApplication.network.status
        val versionLine = textSecondary("Version: ").append(Component.text(version, WHITE))
        val platformLine = textSecondary("Platform: ").append(Component.text(plugin.server.name, WHITE))
            .append(textDark("  |  ")).append(textSecondary("Local tools: ")).append(Component.text("Free", GREEN))
        val networkLine = textSecondary("Network: ").append(Component.text(networkStatus,
            if (networkStatus == "Connected") GREEN else GRAY))
        val message = Component.empty().decoration(TextDecoration.BOLD, false)
            .append(centerComponentLine(textPrimary("Plugin Portal Info").bold()))
            .appendNewline().append(detailLine(versionLine))
            .appendNewline().append(detailLine(platformLine))
            .appendNewline().append(detailLine(networkLine))
        audience.sendMessage(message.boxed())
        // Show local information immediately, even when the update API is unavailable.
        PortalApplication.runtime.executor.execute {
            val update = runCatching { API.checkForPPUpdate(version) }.getOrNull()
            if (update?.updateAvailable == true && update.latest != null) {
                val latest = update.latest
                audience.sendMessage(detailLine(textSecondary("Update available: ").append(Component.text(latest.version, WHITE))
                    .append(Component.text("  [Review]", GREEN))
                    .clickEvent(ClickEvent.suggestCommand("/pp upgrade"))
                    .hoverEvent(HoverEvent.showText(textSecondary("Review the update before downloading")))).boxed())
            }
        }
    }

    private fun detailLine(component: Component): Component =
        centerComponentLine(component.decoration(TextDecoration.BOLD, false))
}
