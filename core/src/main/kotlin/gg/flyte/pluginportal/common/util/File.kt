package gg.flyte.pluginportal.common.util

import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

fun isValidDownload(url: String): Boolean {
    return isJarDownloadUrl(url)
}

fun isJarDownloadUrl(url: String): Boolean {
    if (url.endsWith(".jar")) return true

    try {
        var currentUrl = url
        var redirectCount = 0
        val maxRedirects = 5
        
        while (redirectCount < maxRedirects) {
            val connection = URL(currentUrl).openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            // Add user agent to avoid being blocked
            connection.setRequestProperty("User-Agent", "PluginPortal/1.0")
            
            val responseCode = connection.responseCode
            
            // Handle different response codes
            when (responseCode) {
                200 -> {
                    // Direct download - check headers
                    
                    // Check Content-Type first (most reliable)
                    connection.getHeaderField("Content-Type")?.let { contentType ->
                        when {
                            contentType.contains("java-archive") -> return true
                            contentType == "application/octet-stream" -> {
                                // Binary file - check other headers to confirm it's a JAR
                                connection.getHeaderField("Content-Disposition")?.let { disposition ->
                                    if (disposition.contains(".jar")) return true
                                }
                                connection.getHeaderField("x-bz-file-name")?.let { fileName ->
                                    if (fileName.endsWith(".jar")) return true
                                }
                                // Check Content-Length to ensure it's not a webpage
                                val contentLength = connection.getHeaderField("Content-Length")?.toLongOrNull() ?: 0
                                if (contentLength > 1000) { // JARs are typically larger than 1KB
                                    // Might be a JAR without proper headers
                                    return true
                                }
                            }
                            contentType.contains("text/html") -> return false
                        }
                    }
                    
                    // Check Content-Disposition
                    connection.getHeaderField("Content-Disposition")?.let { disposition ->
                        if (disposition.contains("attachment") && disposition.contains(".jar")) return true
                    }
                    
                    // Backblaze/B2 header
                    connection.getHeaderField("x-bz-file-name")?.let { fileName ->
                        if (fileName.endsWith(".jar")) return true
                    }
                    
                    return false
                }
                
                in 300..399 -> {
                    // Redirect - follow it
                    val location = connection.getHeaderField("Location") ?: return false
                    
                    // Convert relative URLs to absolute
                    currentUrl = if (location.startsWith("http")) {
                        location
                    } else {
                        URL(URL(currentUrl), location).toString()
                    }
                    
                    // Quick checks on the redirect URL
                    when {
                        currentUrl.endsWith(".jar") -> return true
                        currentUrl.contains("/versions") || 
                        currentUrl.contains("/releases") || 
                        currentUrl.contains("/files") -> {
                            // These typically lead to file listing pages, not direct downloads
                            return false
                        }
                    }
                    
                    // Special handling for Spiget external files
                    if (connection.getHeaderField("x-spiget-file-source") == "external") {
                        // If it's a known CI/build server, continue following
                        if (currentUrl.contains("ci.") || 
                            currentUrl.contains("jenkins") || 
                            currentUrl.contains("/job/") ||
                            currentUrl.contains("github.com/.*/.*/releases/download".toRegex())) {
                            redirectCount++
                            continue
                        }
                    }
                    
                    redirectCount++
                    continue
                }
                
                else -> {
                    // Other status codes (4xx, 5xx) indicate errors
                    return false
                }
            }
        }
        
        return false
    } catch (e: Exception) {
        // Don't print stack trace for common errors
        when {
            e.message?.contains("timeout", ignoreCase = true) == true ->
                logger.warning("Download URL validation timed out for $url")
            e.message?.contains("refused", ignoreCase = true) == true ->
                logger.warning("Connection refused while validating download URL for $url")
            else ->
                logger.warning("Error checking download URL for $url: ${e.message ?: e::class.simpleName}")
        }
        return false
    }
}

fun File.isJarFile() = isFile && extension == "jar"

private fun hash(data: ByteArray, algo: String = "SHA-256"): String {
    return MessageDigest
        .getInstance(algo)
        .digest(data)
        .joinToString("") { byte -> "%02x".format(byte) }
}

private fun calculateSHA256(file: File): String = hash(file.readBytes())
private fun calculateSHA1(file: File): String = hash(file.readBytes(), "SHA-1")
private fun calculateSHA512(file: File): String = hash(file.readBytes(), "SHA-512")

enum class HashType(val hash: (File) -> String) {
    SHA256(::calculateSHA256),
    SHA1(::calculateSHA1),
    SHA512(::calculateSHA512)
}
