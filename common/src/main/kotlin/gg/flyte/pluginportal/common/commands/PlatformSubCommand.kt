package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.EnabledCommand
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.commands.lamp.InstalledPluginNotPortalSuggestionProvider
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.notifications.DiscordWebhookNotifier
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.newestCompatibleVersionWithFallback
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.async
import gg.flyte.pluginportal.common.util.currentServerTypePreference
import gg.flyte.pluginportal.common.util.download
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.SuggestWith
import revxrsal.commands.annotation.Switch
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class PlatformSubCommand {

    @EnabledCommand(Features.UPDATE)
    @Subcommand("platform")
    @CommandPermission("pluginportal.maintain.update")
    fun platformCommand(
        audience: Audience,
        @Named("name") @SuggestWith(InstalledPluginNotPortalSuggestionProvider::class) name: String,
        @Named("platform") targetPlatform: MarketplacePlatform,
        @Switch("byId") byId: Boolean = false,
    ) {
        LocalPluginCache.searchPluginsWithFeedback(
            audience,
            name,
            byId,
            ifSingle = { plugin: LocalPlugin -> switchPlatform(audience, plugin, targetPlatform) }.async(),
            ifMore = { plugins: List<LocalPlugin> ->
                sendLocalPluginListMessage(
                    audience,
                    "Multiple plugins found, click one to switch platform",
                    plugins,
                    "platform",
                    " ${targetPlatform.name}"
                )
            },
        )
    }

    companion object {
        fun switchPlatform(audience: Audience, localPlugin: LocalPlugin, targetPlatform: MarketplacePlatform) {
            if (!Config.isDownloadPlatformEnabled(targetPlatform)) {
                audience.sendFailure("Downloading from ${targetPlatform.name} is disabled in config.yml")
                return
            }

            if (localPlugin.platform == targetPlatform) {
                audience.sendInfo("${localPlugin.name} is already tracked on ${targetPlatform.name}.")
                return
            }

            val marketplacePlugin = MarketplacePluginCache.getOrFetchPluginById(localPlugin.platform, localPlugin.platformId)
                ?: return audience.sendFailure("Could not find ${localPlugin.name} in the marketplace.")

            val platformPlugin = marketplacePlugin.platform(targetPlatform)
                ?: return audience.sendFailure("${marketplacePlugin.name} is not available on ${targetPlatform.name}.")

            val serverTypes = currentServerTypePreference()
            val targetVersion = platformPlugin.newestCompatibleVersionWithFallback(localPlugin.preferredChannel, serverTypes) {
                API.getPluginVersions(platformPlugin.platformWithId)?.toList()
            }
                ?: return audience.sendFailure("No compatible version found on ${targetPlatform.name} for ${localPlugin.preferredChannel ?: "the default channel"}.")

            audience.sendInfo("Switching ${localPlugin.name} from ${localPlugin.platform.name} to ${targetPlatform.name}...")

            val newPlugin = marketplacePlugin.download(
                update = true,
                marketplacePlatform = targetPlatform,
                audience = audience,
                version = targetVersion,
                preferredChannel = localPlugin.preferredChannel ?: targetVersion.releaseChannel,
                excludedFromUpdates = localPlugin.excludedFromUpdates,
            ) ?: return audience.sendFailure("Failed to switch ${localPlugin.name} to ${targetPlatform.name}.")

            LocalPluginCache.remove(localPlugin)
            LocalPluginCache.addToUpdatedPluginMap(newPlugin, localPlugin)
            LocalPluginCache.save()

            DiscordWebhookNotifier.managedPluginPlatformSwitched(localPlugin, newPlugin, platformPlugin.webpageURL)
            audience.sendSuccess("${localPlugin.name} now tracks ${targetPlatform.name}. Restart your server for the downloaded jar to take effect.")
        }
    }
}
