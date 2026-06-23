package gg.flyte.pluginportal.common.adapters

import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.compatibleVersions
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.types.Version
import gg.flyte.pluginportal.common.types.PlatformPlugin
import gg.flyte.pluginportal.common.util.ActionResponseString
import gg.flyte.pluginportal.common.util.HashType
import gg.flyte.pluginportal.common.util.currentMinecraftVersion
import gg.flyte.pluginportal.common.util.currentServerTypePreference
import java.io.File

class StandardMarketplaceAdapter : DownloadAdapter {
    override fun getPriority(): Int = 100
    
    override fun canHandle(request: DownloadRequest): Boolean {
        return request.plugin != null && request.url == null && request.plugin.platforms.polymart == null
    }
    
    override fun download(request: DownloadRequest): DownloadResult {
        val plugin = request.plugin ?: return DownloadResult(false, error = "No plugin provided")
        val serverTypes = currentServerTypePreference()
        val minecraftVersion = currentMinecraftVersion()
        
        // Find the right version based on versionFilter (channel)
        val platformVersion = if (request.versionFilter != null) { // TODO: Pass this work onto the endpoint
            // Search through all platforms for versions matching the release channel
            plugin.platforms.asList()
                .filter { gg.flyte.pluginportal.common.Config.isDownloadPlatformEnabled(it.platform) }
                .flatMap { platform -> 
                    platform.versions
                        .compatibleVersions(serverTypes, minecraftVersion)
                        .filter { version -> version.releaseChannel.equals(request.versionFilter, ignoreCase = true) } // TODO: Probably pass this to the endpoint as well
                        .map { version -> platform to version }
                }
                .firstOrNull() // Get the first (newest) version matching the channel
        } else {
            // Default to latest version from best platform
            plugin.platforms.bestDownloadable?.let { platform ->
                platform.newestCompatibleVersion(null, serverTypes, minecraftVersion)?.let { version -> platform to version }
            }
        }
        
        if (platformVersion == null) {
            return DownloadResult(false, error = "No matching version found")
        }
        
        val (platform, version) = platformVersion
        
        if (version.downloadURL == null) {
            return DownloadResult(false, error = "No download URL available for version ${version.versionNumber}")
        }
        
        // For standard downloads with specific versions, we need custom logic
        // since MarketplacePluginCache only downloads latest
//        if (version.downloadURL != null) {
            // Download specific version directly
            val fileName = "${plugin.downloadableName}.jar"
            val targetFile = File(request.targetDirectory, fileName)
            
            try {
                val downloaded = gg.flyte.pluginportal.common.util.download(java.net.URL(version.downloadURL), targetFile, request.audience)
                    ?: return DownloadResult(false, error = "Download failed")
                
                val localPlugin = gg.flyte.pluginportal.common.types.LocalPlugin(
                    entryId = platform.entryId,
                    platformId = platform.platformId,
                    name = plugin.name,
                    version = version.versionNumber,
                    platform = platform.platform,
                    sha256 = HashType.SHA256.hash(downloaded),
                    sha512 = HashType.SHA512.hash(downloaded),
                    installedAt = System.currentTimeMillis(),
                    preferredChannel = request.versionFilter ?: version.releaseChannel,
                )
                
                return DownloadResult(true, downloaded, localPlugin = localPlugin)
            } catch (e: Exception) {
                return DownloadResult(false, error = "Download failed: ${e.message}")
            }
//        }
        
        // Use existing download logic from MarketplacePluginCache for latest version
//        val result = MarketplacePluginCache.installPlugin(
//            request.audience ?: throw IllegalArgumentException("Audience required for marketplace downloads"),
//            plugin,
//            platform.platform,
//            request.targetDirectory
//        )
//
//        return if (result.success) {
//            DownloadResult(true, file = File(request.targetDirectory, "${plugin.downloadableName}.jar"), localPlugin = result.meta)
//        } else {
//            val errorMessage = when (result) {
//                is ActionResponseString -> result.error ?: "Installation failed"
//                else -> "Installation failed"
//            }
//            DownloadResult(false, error = errorMessage)
//        }
    }
}
