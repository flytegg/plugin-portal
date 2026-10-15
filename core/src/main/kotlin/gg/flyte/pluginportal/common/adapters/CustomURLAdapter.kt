package gg.flyte.pluginportal.common.adapters

import gg.flyte.pluginportal.common.util.download
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URL
import java.nio.file.Path

class CustomURLAdapter : DownloadAdapter {
    override fun getPriority(): Int = 50 // Lower priority, fallback option
    
    override fun canHandle(request: DownloadRequest): Boolean {
        return request.url != null
    }
    
    override fun download(request: DownloadRequest): DownloadResult {
        val url = request.url ?: return DownloadResult(false, error = "No URL provided")
        
        // Basic validation
        if (!isValidUrl(url)) {
            return DownloadResult(false, error = "Invalid URL format")
        }
        
        try {
            val client = OkHttpClient.Builder()
                .followRedirects(true)
                .build()
            
            val httpRequest = Request.Builder()
                .url(url)
                .header("User-Agent", "PluginPortal/1.0")
                .build()
            
            // Make a HEAD request first to get headers without downloading the whole file
            val headResponse = client.newCall(httpRequest.newBuilder().head().build()).execute()
            if (!headResponse.isSuccessful) {
                headResponse.close()
                return DownloadResult(false, error = "Download URL did not return a successful response")
            }
            
            val finalUrl = headResponse.request.url.toString()
            val contentDisposition = headResponse.header("Content-Disposition")
            if (headResponse.request.url.scheme != "https") {
                headResponse.close()
                return DownloadResult(false, error = "Redirected download URL must use HTTPS")
            }
            
            // Extract filename from Content-Disposition header
            val fileName = contentDisposition?.let { parseContentDisposition(it) }
                ?: finalUrl.substringAfterLast('/').takeIf { it.contains('.') }
                ?: "custom-plugin-${System.currentTimeMillis()}.jar"
            val safeFileName = sanitizeFileName(fileName)
            if (safeFileName == null) {
                headResponse.close()
                return DownloadResult(false, error = "Unsafe download filename")
            }
            
            headResponse.close()
            
            val targetFile = File(request.targetDirectory, safeFileName)
            val targetRoot = request.targetDirectory.canonicalFile.toPath()
            val targetPath = targetFile.canonicalFile.toPath()
            if (!targetPath.startsWith(targetRoot)) {
                return DownloadResult(false, error = "Unsafe download path")
            }
            
            // Download the file
            val downloaded = download(URL(finalUrl), targetFile, request.audience)
                ?: return DownloadResult(false, error = "Download failed")
            
            // Verify it's a valid jar/zip
            if (!isValidJarFile(downloaded)) {
                downloaded.delete()
                return DownloadResult(false, error = "Downloaded file is not a valid JAR/ZIP file")
            }
            
            // Don't create LocalPlugin entry for custom downloads
            // They shouldn't be tracked in the plugin cache
            return DownloadResult(true, targetFile, localPlugin = null)
            
        } catch (e: Exception) {
            return DownloadResult(false, error = "Download failed: ${e.message}")
        }
    }
    
    private fun isValidUrl(url: String): Boolean {
        return try {
            val u = URL(url)
            u.protocol == "https"
        } catch (e: Exception) {
            false
        }
    }

    private fun sanitizeFileName(fileName: String): String? {
        val decoded = try {
            java.net.URLDecoder.decode(fileName.substringBefore("?"), "UTF-8")
        } catch (e: Exception) {
            fileName.substringBefore("?")
        }
        val normalized = decoded.replace('\\', '/').substringAfterLast('/').trim()
        if (normalized.isBlank() || normalized == "." || normalized == "..") return null
        if (normalized.contains("..") || Path.of(normalized).nameCount != 1) return null
        if (!normalized.endsWith(".jar", ignoreCase = true)) return null
        return normalized
    }
    
    private fun isValidJarFile(file: File): Boolean {
        return try {
            // Check if it's a valid ZIP/JAR by trying to read it
            java.util.zip.ZipFile(file).use { zip ->
                zip.entries().hasMoreElements()
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun parseContentDisposition(contentDisposition: String): String? {
        // Handle both regular filename and RFC 2231 encoded filename*
        // Examples:
        // attachment; filename="file.jar"
        // attachment; filename*=UTF-8''file%20name.jar
        // attachment; filename="file.jar"; filename*=UTF-8''better%20name.jar
        
        // First try to find filename* (RFC 2231 encoded)
        val rfc2231Regex = """filename\*\s*=\s*(?:([^']*)'[^']*')?([^;\s]+)""".toRegex()
        rfc2231Regex.find(contentDisposition)?.let { match ->
            val encodedName = match.groupValues[2]
            // URL decode the filename
            return try {
                java.net.URLDecoder.decode(encodedName, "UTF-8")
            } catch (e: Exception) {
                encodedName
            }
        }
        
        // Fall back to regular filename
        val regularRegex = """filename\s*=\s*"?([^";\s]+)"?""".toRegex()
        return regularRegex.find(contentDisposition)?.groupValues?.get(1)
    }
}
