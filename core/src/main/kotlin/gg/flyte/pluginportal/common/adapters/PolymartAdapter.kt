package gg.flyte.pluginportal.common.adapters

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.HashType
import gg.flyte.pluginportal.common.util.download
import gg.flyte.pluginportal.common.util.getPolymartDownloadUrl
import java.io.File
import java.net.URL

class PolymartAdapter : DownloadAdapter {
    override fun getPriority(): Int = 200
    
    override fun canHandle(request: DownloadRequest): Boolean {
        return request.plugin?.platforms?.polymart != null
    }
    
    override fun download(request: DownloadRequest): DownloadResult {
        val plugin = request.plugin ?: return DownloadResult(false, error = "No plugin provided")
        val polymart = plugin.platforms.polymart ?: return DownloadResult(false, error = "Not a Polymart plugin")
        
        // Check if it's a premium plugin
        val isPremium = polymart.premium != null
        
        // Disable premium plugin downloads
        if (isPremium) {
            return DownloadResult(
                false,
                error = "Downloading premium Polymart plugins is currently not supported"
            )
        }
        
        // For free plugins, we can use the direct download URL
        try {
            val downloadUrl = getPolymartDownloadUrl(polymart.platformId)
                ?: return DownloadResult(false, error = "Failed to get download URL from Polymart API")
            
            // Download the file
            val fileName = "[PP] ${plugin.name} (POLYMART).jar"
            val targetFile = File(request.targetDirectory, fileName)
            
            val downloaded = download(URL(downloadUrl), targetFile, request.audience)
                ?: return DownloadResult(false, error = "Download failed")
            
            // Create LocalPlugin entry
            val localPlugin = LocalPlugin(
                entryId = polymart.entryId,
                platformId = polymart.platformId,
                name = plugin.name,
                version = polymart.latestVersion?.versionNumber ?: "unknown",
                platform = MarketplacePlatform.POLYMART,
                sha256 = HashType.SHA256.hash(downloaded),
                sha512 = HashType.SHA512.hash(downloaded),
                installedAt = System.currentTimeMillis()
            )
            
            return DownloadResult(true, downloaded, localPlugin = localPlugin)
        } catch (e: Exception) {
            return DownloadResult(false, error = "Download failed: ${e.message}")
        }
    }
}
