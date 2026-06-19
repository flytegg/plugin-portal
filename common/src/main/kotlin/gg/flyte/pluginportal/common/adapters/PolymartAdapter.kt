package gg.flyte.pluginportal.common.adapters

import com.google.gson.JsonObject
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.HashType
import gg.flyte.pluginportal.common.util.download
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

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
            val downloadUrl = getPolymartDownloadUrl(polymart.platformId, null, isPremium)
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
    
    
    private fun getPolymartDownloadUrl(resourceId: String, token: String?, isPremium: Boolean): String? {
        return try {
            val url = URL("https://api.polymart.org/v1/getDownloadURL")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            
            val postData = buildString {
                append("resource_id=${URLEncoder.encode(resourceId, "UTF-8")}")
                // Only add token parameter if it's not null and not blank
                // For free resources, we should NOT send any token parameter at all
                if (!token.isNullOrBlank()) {
                    // For premium resources, this must be a valid OAuth token
                    // obtained through the user authorization flow
                    append("&token=${URLEncoder.encode(token, "UTF-8")}")
                }
            }

            PluginPortalBase.plugin.logger.info(
                "Requesting Polymart download URL from $url for resource $resourceId (${if (isPremium) "premium" else "free"})"
            )
            connection.outputStream.use { it.write(postData.toByteArray()) }
            
            val responseCode = connection.responseCode
            
            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = GSON.fromJson(response, JsonObject::class.java)
                
                if (json.getAsJsonObject("response")?.get("success")?.asBoolean == true) {
                    return json.getAsJsonObject("response")
                        ?.getAsJsonObject("result")
                        ?.get("url")?.asString
                } else {
                    val error = json.getAsJsonObject("response")?.get("message")?.asString
                    PluginPortalBase.plugin.logger.warning("Polymart download URL request failed: ${error ?: "unknown error"}")
                }
            } else {
                PluginPortalBase.plugin.logger.warning("Polymart download URL request failed with HTTP $responseCode")
            }
            null
        } catch (e: Exception) {
            PluginPortalBase.plugin.logger.warning("Polymart download URL request failed: ${e.message ?: e::class.simpleName}")
            null
        }
    }
    
}
