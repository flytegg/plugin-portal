package gg.flyte.pluginportal.common.types

import com.google.gson.JsonObject
import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.logging.PortalLogger
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.managers.LocalPluginCache.installUpdate
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.managers.PluginModificationManager
import gg.flyte.pluginportal.common.notifications.DiscordWebhookNotifier
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.ActionResponseString
import gg.flyte.pluginportal.common.util.currentMinecraftVersion
import gg.flyte.pluginportal.common.util.currentServerTypePreference
import gg.flyte.pluginportal.common.util.download
import net.kyori.adventure.audience.Audience

class SocketActions {
    val install = SocketAction("install") { data: JsonObject ->
        val platform = data.get("platform")?.asString ?: return@SocketAction PluginActionResponse(false, "Missing platform")
        val id = data.get("id")?.asString ?: return@SocketAction PluginActionResponse(false, "Missing id")
        val versionNumber = data.get("version")?.asString?.takeIf { it.isNotBlank() }
        val channel = data.get("channel")?.asString?.takeIf { it.isNotBlank() }

        val marketplacePlatform = MarketplacePlatform.of(platform)
            ?: return@SocketAction PluginActionResponse(false, "Invalid platform: $platform")
        if (!Config.isDownloadPlatformEnabled(marketplacePlatform)) {
            return@SocketAction PluginActionResponse(false, "Downloading from ${marketplacePlatform.name} is disabled in config.yml")
        }
        
        val plugin = MarketplacePluginCache.getOrFetchPluginById(marketplacePlatform, id)
            ?: return@SocketAction PluginActionResponse(false, "Plugin not found")

        val platformPlugin = plugin.platform(marketplacePlatform)
            ?: return@SocketAction PluginActionResponse(false, "Plugin not found on $marketplacePlatform")
        val serverTypes = currentServerTypePreference()
        val minecraftVersion = currentMinecraftVersion()
        val version = if (versionNumber != null) {
            val selection = platformPlugin.exactCompatibleVersionWithFallback(versionNumber, channel, serverTypes, minecraftVersion) {
                API.getPluginVersions(platformPlugin.platformWithId)?.toList()
            }

            when (selection) {
                is ExactVersionSelection.Found -> selection.version
                is ExactVersionSelection.Ambiguous ->
                    return@SocketAction PluginActionResponse(false, "Multiple compatible versions named $versionNumber exist. Select a release channel.")
                ExactVersionSelection.NotFound ->
                    return@SocketAction PluginActionResponse(false, "No compatible version found for $versionNumber")
            }
        } else {
            platformPlugin.newestCompatibleVersionWithFallback(channel, serverTypes, minecraftVersion) {
                API.getPluginVersions(platformPlugin.platformWithId)?.toList()
            }
                ?: return@SocketAction PluginActionResponse(false, "No compatible version found")
        }

        val localPlugin = plugin.download(
            update = false,
            marketplacePlatform = marketplacePlatform,
            audience = Audience.empty(),
            version = version,
            preferredChannel = channel ?: version.releaseChannel,
        )
        val response = ActionResponseString(localPlugin != null, if (localPlugin == null) "Installation failed" else null, localPlugin)
        
        val errorMessage = if (!response.success) {
            PortalLogger.warn("Install failed for $platform $id: ${response.error ?: "unknown error"}")
            response.error ?: "Installation failed"
        } else null
        
        PluginActionResponse(response.success, errorMessage)
    }

    val uninstall = SocketAction("uninstall") { data: JsonObject ->
        val platform = data.get("platform")?.asString ?: return@SocketAction PluginActionResponse(false, "Missing platform")
        val id = data.get("id")?.asString ?: return@SocketAction PluginActionResponse(false, "Missing id")

        val marketplacePlatform = MarketplacePlatform.of(platform)
            ?: return@SocketAction PluginActionResponse(false, "Invalid platform: $platform")
            
        val localPlugin = LocalPluginCache.find { it.platformId == id && it.platform == marketplacePlatform }
            ?: return@SocketAction PluginActionResponse(false, "Plugin not installed")

        val response = PluginModificationManager.uninstall(Audience.empty(), localPlugin)
        val errorMessage = if (!response.success && response is ActionResponseString<*>) {
            PortalLogger.warn("Uninstall failed for $platform $id: ${response.error ?: "unknown error"}")
            response.error ?: "Uninstallation failed"
        } else null
        PluginActionResponse(response.success, errorMessage)
    }

    val update = SocketAction("update") { data: JsonObject ->
        val platform = data.get("platform")?.asString ?: return@SocketAction PluginActionResponse(false, "Missing platform")
        val id = data.get("id")?.asString ?: return@SocketAction PluginActionResponse(false, "Missing id")

        val marketplacePlatform = MarketplacePlatform.of(platform)
            ?: return@SocketAction PluginActionResponse(false, "Invalid platform: $platform")
            
        val localPlugin = LocalPluginCache.find { it.platformId == id && it.platform == marketplacePlatform }
            ?: return@SocketAction PluginActionResponse(false, "Plugin not installed")
            
        // Use the built-in update method from LocalPluginCache
        val response = localPlugin.installUpdate(Audience.empty())
        val errorMessage = if (!response.success && response is ActionResponseString) {
            PortalLogger.warn("Update failed for $platform $id: ${response.error ?: "unknown error"}")
            response.error ?: "Update failed"
        } else null
        PluginActionResponse(response.success, errorMessage)
    }

    val toggleBlacklist = SocketAction("toggleBlacklist") { data: JsonObject ->
        val platform = data.get("platform")?.asString ?: return@SocketAction PluginActionResponse(false, "Missing platform")
        val id = data.get("id")?.asString ?: return@SocketAction PluginActionResponse(false, "Missing id")

        val marketplacePlatform = MarketplacePlatform.of(platform)
            ?: return@SocketAction PluginActionResponse(false, "Invalid platform: $platform")

        val localPlugin = LocalPluginCache.find { it.platformId == id && it.platform == marketplacePlatform }
            ?: return@SocketAction PluginActionResponse(false, "Plugin not installed")

        localPlugin.excludedFromUpdates = !localPlugin.excludedFromUpdates
        LocalPluginCache.save()
        PluginActionResponse(true)
    }

    val switchPlatform = SocketAction("switchPlatform") { data: JsonObject ->
        val platform = data.get("platform")?.asString ?: return@SocketAction PluginActionResponse(false, "Missing platform")
        val id = data.get("id")?.asString ?: return@SocketAction PluginActionResponse(false, "Missing id")
        val target = data.get("targetPlatform")?.asString ?: return@SocketAction PluginActionResponse(false, "Missing target platform")

        val marketplacePlatform = MarketplacePlatform.of(platform)
            ?: return@SocketAction PluginActionResponse(false, "Invalid platform: $platform")
        val targetPlatform = MarketplacePlatform.of(target)
            ?: return@SocketAction PluginActionResponse(false, "Invalid platform: $target")
        if (!Config.isDownloadPlatformEnabled(targetPlatform)) {
            return@SocketAction PluginActionResponse(false, "Downloading from ${targetPlatform.name} is disabled in config.yml")
        }

        val localPlugin = LocalPluginCache.find { it.platformId == id && it.platform == marketplacePlatform }
            ?: return@SocketAction PluginActionResponse(false, "Plugin not installed")

        val marketplacePlugin = MarketplacePluginCache.getOrFetchPluginById(localPlugin.platform, localPlugin.platformId)
            ?: return@SocketAction PluginActionResponse(false, "Plugin not found")
        val platformPlugin = marketplacePlugin.platform(targetPlatform)
            ?: return@SocketAction PluginActionResponse(false, "${marketplacePlugin.name} is not available on $targetPlatform")
        val serverTypes = currentServerTypePreference()
        val minecraftVersion = currentMinecraftVersion()
        val version = platformPlugin.newestCompatibleVersionWithFallback(localPlugin.preferredChannel, serverTypes, minecraftVersion) {
            API.getPluginVersions(platformPlugin.platformWithId)?.toList()
        }
            ?: return@SocketAction PluginActionResponse(false, "No compatible version found")

        val newPlugin = marketplacePlugin.download(
            update = true,
            marketplacePlatform = targetPlatform,
            audience = Audience.empty(),
            version = version,
            preferredChannel = localPlugin.preferredChannel ?: version.releaseChannel,
            excludedFromUpdates = localPlugin.excludedFromUpdates,
        ) ?: return@SocketAction PluginActionResponse(false, "Platform switch failed")

        LocalPluginCache.remove(localPlugin)
        LocalPluginCache.addToUpdatedPluginMap(newPlugin, localPlugin)
        LocalPluginCache.save()
        DiscordWebhookNotifier.managedPluginPlatformSwitched(localPlugin, newPlugin, platformPlugin.webpageURL)
        PluginActionResponse(true)
    }
}

class SocketAction<T>(
    val name: String, 
    private val execute: (T) -> PluginActionResponse
) {
    fun run(data: T): PluginActionResponse = execute(data)
}
