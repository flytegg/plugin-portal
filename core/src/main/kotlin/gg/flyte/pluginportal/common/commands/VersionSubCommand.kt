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
        var message: Component = Component.empty().append(textPrimary("Plugin Portal Info").bold())
            .appendNewline().append(textSecondary("Version  "))
            .append(Component.text(version, if ('-' in version) YELLOW else GREEN))
            .appendNewline().append(textSecondary("Platform  ")).append(textPrimary(plugin.server.name))
            .appendNewline().append(textSecondary("Local tools  ")).append(Component.text("Free", GREEN))
            .appendNewline().append(textSecondary("Network  ")).append(textPrimary(PortalApplication.network.status))
        audience.sendMessage(message.boxed())
        // Show local information immediately, even when the update API is unavailable.
        PortalApplication.runtime.executor.execute {
            val update = runCatching { API.checkForPPUpdate(version) }.getOrNull()
            if (update?.updateAvailable == true && update.latest != null) {
                val latest = update.latest
                audience.sendMessage(textSecondary("Update available  ").append(textPrimary(latest.version))
                    .append(textPrimary("  [Review]"))
                    .clickEvent(ClickEvent.suggestCommand("/pp upgrade"))
                    .hoverEvent(HoverEvent.showText(textSecondary("Review the update before downloading"))).boxed())
            }
        }
    }
}
