package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.Features
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration
import kotlin.concurrent.thread

@Command("pp", "pluginportal", "ppm")
@CommandPermission("pluginportal.view")
class VersionSubCommand {

    @Subcommand("info", "version")
    @CommandPermission("pluginportal.view")
    fun onCommand(audience: Audience) {
        val plugin = PluginPortalBase.plugin
        val currentVersion = plugin.description.version
        
        // Parse current version to get channel
        val versionParts = currentVersion.split("-", limit = 2)
        val baseVersion = versionParts[0]
        val currentChannel = versionParts.getOrNull(1) ?: "stable"
        
        // Get channel color
        val channelColor = when(currentChannel) {
            "stable" -> GREEN
            "rc" -> GOLD
            "beta" -> YELLOW
            "alpha" -> RED
            else -> GRAY
        }
        
        thread {
            try {
                val updateCheck = API.checkForPPUpdate(currentVersion)
            
                // Build the message without TextComponent.Builder.build(), whose JVM
                // signature differs between Adventure 4 and 5.
                var message = Component.empty()
                
                // Header
                message = message.append(
                    centerComponentLine(
                        text("Plugin Portal Info", AQUA, TextDecoration.BOLD)
                    )
                )
                .appendNewline()
                
                // Current version and latest version on same line if update available
                if (updateCheck != null && updateCheck.latest != null) {
                    val latest = updateCheck.latest
                    val latestChannelColor = when(latest.channel) {
                        "stable" -> GREEN
                        "rc" -> GOLD
                        "beta" -> YELLOW
                        "alpha" -> RED
                        else -> GRAY
                    }
                    
                    message = message.append(
                        centerComponentLine(
                            Component.empty()
                                .append(text("Current: ", GRAY))
                                .append(text(baseVersion, WHITE))
                                .append(text(" (", DARK_GRAY))
                                .append(text(currentChannel.uppercase(), channelColor))
                                .append(text(")", DARK_GRAY))
                                .append(text("  →  ", DARK_GRAY))
                                .append(text("Latest: ", GRAY))
                                .append(text(latest.version, WHITE))
                                .append(text(" (", DARK_GRAY))
                                .append(text(latest.channel.uppercase(), latestChannelColor))
                                .append(text(")", DARK_GRAY))
                        )
                    )
                } else {
                    message = message.append(
                        centerComponentLine(
                            Component.empty()
                                .append(text("Version: ", GRAY))
                                .append(text(baseVersion, WHITE))
                                .append(text(" (", DARK_GRAY))
                                .append(text(currentChannel.uppercase(), channelColor))
                                .append(text(")", DARK_GRAY))
                        )
                    )
                }
                message = message.appendNewline()
                
                // Artifact and entitlement on same line
                message = message.append(
                    centerComponentLine(
                        Component.empty()
                            .append(text("Artifact: ", GRAY))
                            .append(text("PluginPortal", WHITE))
                            .append(text("  |  ", DARK_GRAY))
                            .append(text("Premium: ", GRAY))
                            .append(text(
                                if (PluginPortalBase.info.hasPremiumEntitlement()) "Unlocked" else "Locked",
                                if (PluginPortalBase.info.hasPremiumEntitlement()) GREEN else RED
                            ))
                    )
                )
                
                // Update Status
                if (updateCheck != null) {
                    if (updateCheck.updateAvailable && updateCheck.latest != null) {
                        if (!Features.AUTOMATICALLY_UPDATE_PPP.isEnabled()) {
                            message = message.appendNewline()
                            .append(
                                centerComponentLine(
                                    Component.empty()
                                        .append(text("[", DARK_GRAY))
                                        .append(
                                            text("CLICK TO UPDATE", GREEN, TextDecoration.UNDERLINED)
                                                .clickEvent(ClickEvent.runCommand("/pp update"))
                                                .hoverEvent(HoverEvent.showText(text("Run /pp update", GREEN)))
                                        )
                                        .append(text("]", DARK_GRAY))
                                )
                            )
                        }
                    }
                }
                
                // Send the message
                audience.sendMessage(message.boxed())
                
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
}
