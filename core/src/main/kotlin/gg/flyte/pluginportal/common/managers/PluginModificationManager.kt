package gg.flyte.pluginportal.common.managers

import gg.flyte.pluginportal.common.Constants
import gg.flyte.pluginportal.common.adapters.DownloadManager
import gg.flyte.pluginportal.common.adapters.DownloadRequest
import gg.flyte.pluginportal.common.logging.PortalLogger
import gg.flyte.pluginportal.common.managers.LocalPluginCache.findFile
import gg.flyte.pluginportal.common.managers.LocalPluginCache.popCurrentVersionFile
import gg.flyte.pluginportal.common.notifications.DiscordWebhookNotifier
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.util.ActionResponse
import gg.flyte.pluginportal.common.util.ActionResponseString
import gg.flyte.pluginportal.common.util.isPluginPortal
import gg.flyte.pluginportal.common.util.isPluginPortalMarketplaceEntry
import net.kyori.adventure.audience.Audience

object PluginModificationManager {
    @Deprecated("Use Plugin#download instead")
    fun install(audience: Audience, plugin: Plugin, platformFlag: MarketplacePlatform?): ActionResponse<LocalPlugin> {
        if (LocalPluginCache.hasPlugin(plugin)) {
            return ActionResponseString(false, "Plugin already installed, use the update command instead")
        }

        if (plugin.isPluginPortalMarketplaceEntry) {
            return ActionResponseString(false, "Plugin Portal is already installed")
        }

        val platform = platformFlag ?: plugin.bestPlatform ?: return ActionResponseString(false, "We couldn't find a valid platform!")

        // Use the new download adapter system
        val request = DownloadRequest(
            plugin = plugin,
            targetDirectory = Constants.INSTALL_DIRECTORY,
            audience = audience
        )
        
        val result = DownloadManager.download(request)
        
        return if (result.success) {
            ActionResponseString(true, "Successfully installed ${plugin.name}", result.localPlugin)
        } else {
            ActionResponseString(false, result.error ?: "Installation failed")
        }
    }
    fun uninstall(audience: Audience, localPlugin: LocalPlugin): ActionResponse<Nothing> {
        val targetPlatform = localPlugin.platform
        val targetMessage = "${localPlugin.name} from $targetPlatform with ID ${localPlugin.platformId}"

        if (localPlugin.isPluginPortal) {
            return ActionResponseString(false, "You cannot delete Plugin Portal")
        }

        val file = localPlugin.findFile()
        val otherFile = localPlugin.popCurrentVersionFile()
        val files = listOf(file, otherFile)

        if (file == null && otherFile == null) {
            LocalPluginCache.remove(localPlugin)
            LocalPluginCache.save()
            return ActionResponseString(false, "Could not find plugin jar to delete")
        }

        val failedDeletes = LocalPluginCache.deletePlugin(localPlugin, files)
        if (failedDeletes.isNotEmpty()) {
            val fileNames = failedDeletes.joinToString(", ") { it.file.name }
            val failures = failedDeletes.joinToString("; ") { "${it.file.path}: ${it.reason}" }
            PortalLogger.warn("Failed to delete plugin jar for $targetMessage: $failures")
            return ActionResponseString(false, "Could not delete plugin jar: $fileNames. Stop the server and delete it manually.")
        }

        PortalLogger.log(audience, PortalLogger.Action.DELETE, targetMessage)
        DiscordWebhookNotifier.managedPluginUninstalled(localPlugin, localPlugin.marketplaceUrlOrNull())
        return ActionResponseString(true, "Successfully deleted ${localPlugin.name}")
    }

    private fun LocalPlugin.marketplaceUrlOrNull(): String? =
        runCatching { marketplacePlugin.platform(platform)?.webpageURL }.getOrNull()
}
