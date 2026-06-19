package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.LatestVersion
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.ReleaseChannelSuggestionProvider
import gg.flyte.pluginportal.common.managers.PluginPortalSelfUpdateManager
import gg.flyte.pluginportal.common.managers.PluginPortalSelfUpdateManager.AvailableUpdate
import gg.flyte.pluginportal.common.notifications.DiscordWebhookNotifier
import gg.flyte.pluginportal.common.types.Version
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Flag
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.SuggestWith
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.Switch
import revxrsal.commands.bukkit.annotation.CommandPermission
import kotlin.concurrent.thread

@Command("pp", "pluginportal", "ppm")
@CommandPermission("pluginportal.admin")
class UpgradeSubCommand {
    @Subcommand("upgrade")
    @CommandPermission("pluginportal.admin")
    fun onCommand(
        audience: Audience,
        @Optional @Switch("yes") confirm: Boolean,
        @Optional @Flag("channel") @SuggestWith(ReleaseChannelSuggestionProvider::class) channel: String? = null,
    ) {
        val plugin = PluginPortalBase.plugin
        val currentVersion = plugin.description.version
        val currentChannel = currentVersion.split("-", limit = 2).getOrNull(1) ?: "release"
        val requestedChannel = PluginPortalSelfUpdateManager.normalizeChannel(channel)

        audience.sendInfo("Checking for updates...")
        
        thread {
            try {
                val marketplaceUpdate = PluginPortalSelfUpdateManager.findMarketplaceUpdate(currentVersion, requestedChannel)

                if (marketplaceUpdate != null) {
                    if (confirm) {
                        performMarketplaceUpgrade(audience, marketplaceUpdate)
                    } else {
                        showAvailableUpdates(
                            audience,
                            currentVersion,
                            currentChannel,
                            requestedChannel,
                            listOf(marketplaceUpdate.targetVersion.toLatestVersion())
                        )
                    }
                    return@thread
                }

                val canonicalPlugin = PluginPortalSelfUpdateManager.fetchCanonicalPlugin()
                if (canonicalPlugin != null) {
                    val target = PluginPortalSelfUpdateManager.findMarketplaceTarget(requestedChannel)
                    if (target == null) {
                        return@thread audience.sendFailure("No compatible Plugin Portal version found on channel $requestedChannel.")
                    }
                    return@thread audience.sendSuccess("Plugin Portal is already up to date on channel $requestedChannel!")
                }

                if (requestedChannel != PluginPortalSelfUpdateManager.DEFAULT_CHANNEL) {
                    return@thread audience.sendFailure("Could not check channel $requestedChannel because the Plugin Portal marketplace entry is unavailable.")
                }

                val updateCheck = API.checkForPPUpdate(currentVersion)
                    ?: return@thread audience.sendFailure("Failed to check for updates. Please check your internet connection.")

                val latest = updateCheck.latest
                if (!updateCheck.updateAvailable || latest == null) {
                    return@thread audience.sendSuccess("Plugin Portal is already up to date!")
                }
                
                if (confirm) {
                    performLegacyUpgrade(audience, latest)
                } else {
                    showAvailableUpdates(audience, currentVersion, currentChannel, requestedChannel, listOf(latest))
                }
                
            } catch (e: Exception) {
                val reason = e.message ?: e::class.simpleName
                audience.sendFailure("An error occurred while checking for updates: $reason")
                PluginPortalBase.plugin.logger.warning("Failed to check for Plugin Portal updates: $reason")
            }
        }
    }
    
    private fun showAvailableUpdates(
        audience: Audience,
        currentVersion: String,
        currentChannel: String,
        requestedChannel: String,
        versions: List<LatestVersion>
    ) {
        val messageBuilder = text()
        
        // Header
        messageBuilder.append(startLine())
        messageBuilder.append(
            centerComponentLine(
                text("Plugin Portal Upgrade", AQUA, TextDecoration.BOLD)
            )
        )
        messageBuilder.appendNewline()
        
        // Current version
        messageBuilder.append(
            centerComponentLine(
                text()
                    .append(text("Current Version: ", GRAY))
                    .append(text(currentVersion.substringBefore("-"), WHITE))
                    .append(text(" (", DARK_GRAY))
                    .append(text(currentChannel.uppercase(), getChannelColor(currentChannel)))
                    .append(text(")", DARK_GRAY))
                    .build()
            )
        )
        messageBuilder.appendNewline()
        
        // Available updates header
        messageBuilder.append(
            centerComponentLine(
                text("Available Updates:", YELLOW)
            )
        )
        messageBuilder.appendNewline()
        
        // List each available version
        versions.sortedByDescending { getChannelPriority(it.channel) }.forEach { version ->
            val versionString = if (!version.channel.equals("release", ignoreCase = true) && !version.channel.equals("stable", ignoreCase = true)) {
                "${version.version}-${version.channel}"
            } else {
                version.version
            }
            
            // Version header with arrow
            messageBuilder.append(text("  ", DARK_GRAY))
            messageBuilder.append(text("→ ", GOLD))
            messageBuilder.append(text(version.version, WHITE, TextDecoration.BOLD))
            messageBuilder.append(text(" (", DARK_GRAY))
            messageBuilder.append(text(version.channel.uppercase(), getChannelColor(version.channel)))
            messageBuilder.append(text(")", DARK_GRAY))
            messageBuilder.appendNewline()
            
            // Changelog if available
            val changelog = version.changelog
            if (changelog != null) {
                changelog.split("\n").forEach { line ->
                    messageBuilder.append(text("    ", DARK_GRAY))
                    messageBuilder.append(text(line, GRAY))
                    messageBuilder.appendNewline()
                }
            }
            messageBuilder.appendNewline()
        }
        
        // Upgrade button for the latest stable or specified channel
        val recommendedVersion = versions.find { it.channel.equals("release", ignoreCase = true) || it.channel.equals("stable", ignoreCase = true) } ?: versions.first()
        val channelSuffix = if (requestedChannel == PluginPortalSelfUpdateManager.DEFAULT_CHANNEL) "" else " --channel $requestedChannel"
        val upgradeCommand = "/pp upgrade --yes$channelSuffix"
        
        if (audience.isConsole()) {
            // For console, show the command to run
            messageBuilder.append(
                centerComponentLine(
                    text()
                        .append(text("To upgrade, run: ", GRAY))
                        .append(text(upgradeCommand, GREEN, TextDecoration.BOLD))
                        .build()
                )
            )
        } else {
            // For players, show clickable button
            messageBuilder.append(
                centerComponentLine(
                    text()
                        .append(text("[", DARK_GRAY))
                        .append(
                            text("CLICK TO UPGRADE TO ${recommendedVersion.version}", GREEN, TextDecoration.UNDERLINED)
                                .clickEvent(ClickEvent.runCommand(upgradeCommand))
                                .hoverEvent(HoverEvent.showText(text("Run $upgradeCommand", GREEN)))
                        )
                        .append(text("]", DARK_GRAY))
                        .build()
                )
            )
        }
        messageBuilder.appendNewline()
        
        messageBuilder.append(endLine())
        
        audience.sendMessage(messageBuilder.build())
    }
    
    private fun performMarketplaceUpgrade(audience: Audience, update: AvailableUpdate) {
        val versionString = update.versionString
        
        audience.sendMessage(
            text()
                .append(startLine())
                .append(text("[PluginPortal] ", DARK_GRAY))
                .append(text("Starting upgrade to ", GRAY))
                .append(text(versionString, AQUA, TextDecoration.BOLD))
                .append(text("...", GRAY))
                .appendNewline()
                .append(endLine())
                .build()
        )
        
        try {
            val currentVersion = PluginPortalBase.plugin.description.version
            PluginPortalBase.plugin.logger.info("Downloading Plugin Portal update $currentVersion -> $versionString")
            val downloaded = PluginPortalSelfUpdateManager.downloadMarketplaceUpdate(update, audience)

            if (!downloaded) {
                PluginPortalBase.plugin.logger.warning("Marketplace Plugin Portal update failed; trying legacy update endpoint.")
                return performLegacyUpgrade(audience, update.targetVersion.toLatestVersion())
            }

            PluginPortalBase.plugin.logger.info("Successfully downloaded Plugin Portal v$versionString")
            DiscordWebhookNotifier.pluginPortalUpdated(currentVersion, versionString, automatic = false)
            sendUpgradeSuccess(audience, versionString)
        } catch (e: Exception) {
            val reason = e.message ?: e::class.simpleName
            audience.sendFailure("Failed to download update: $reason")
            PluginPortalBase.plugin.logger.warning("Failed to download Plugin Portal update: $reason")
        }
    }

    private fun performLegacyUpgrade(audience: Audience, targetVersion: LatestVersion) {
        val versionString = targetVersion.version

        try {
            PluginPortalBase.plugin.logger.info("Downloading Plugin Portal update ${PluginPortalBase.plugin.description.version} -> $versionString")
            val result = API.downloadPluginPortalUpdate(versionString, targetVersion.channel.toLegacyUpdateChannel())

            if (!result) return audience.sendFailure("Failed to download update. See console for more details.")

            PluginPortalBase.plugin.logger.info("Successfully downloaded Plugin Portal v$versionString")
            DiscordWebhookNotifier.pluginPortalUpdated(PluginPortalBase.plugin.description.version, versionString, automatic = false)
            sendUpgradeSuccess(audience, versionString)
        } catch (e: Exception) {
            val reason = e.message ?: e::class.simpleName
            audience.sendFailure("Failed to download update: $reason")
            PluginPortalBase.plugin.logger.warning("Failed to download Plugin Portal update: $reason")
        }
    }

    private fun sendUpgradeSuccess(audience: Audience, versionString: String) {
        audience.sendMessage(
            text()
                .append(startLine())
                .append(text("[SUCCESS] ", GREEN, TextDecoration.BOLD))
                .append(text("Plugin Portal has been upgraded to ", GRAY))
                .append(text(versionString, AQUA))
                .appendNewline()
                .appendNewline()
                .append(text("  ", DARK_GRAY))
                .append(text("Please restart your server to apply the update.", YELLOW))
                .appendNewline()
                .append(endLine())
                .build()
        )
    }
    
    private fun getChannelColor(channel: String) = when(channel) {
        "stable", "release" -> GREEN
        "rc" -> GOLD
        "beta" -> YELLOW
        "alpha" -> RED
        else -> GRAY
    }
    
    private fun getChannelPriority(channel: String) = when(channel) {
        "stable", "release" -> 4
        "rc" -> 3
        "beta" -> 2
        "alpha" -> 1
        else -> 0
    }

    private fun Version.toLatestVersion() = LatestVersion(
        version = versionNumber,
        channel = releaseChannel ?: "release",
        downloadUrl = downloadURL ?: "",
        changelog = null
    )

    private fun String.toLegacyUpdateChannel(): String =
        if (equals("release", ignoreCase = true)) "stable" else this
}
