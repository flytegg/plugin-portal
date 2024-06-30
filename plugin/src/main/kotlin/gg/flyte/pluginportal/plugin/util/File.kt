package gg.flyte.pluginportal.plugin.util

import gg.flyte.pluginportal.plugin.PluginPortal
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

fun isValidDownload(url: String): Boolean {
    return isJarDownloadUrl(url)
}

fun isJarDownloadUrl(url: String): Boolean {
    if (url.endsWith(".jar")) return true

    // Check if the header "x-bz-file-name" is present and ends with ".jar"
    val connection = runCatching { URL(url).openConnection() as HttpURLConnection }.getOrNull() ?: return false
    val fileName = connection.getHeaderField("x-bz-file-name") ?: return false
    if (fileName.endsWith(".jar")) return true

    connection.instanceFollowRedirects = false
    val contentDisposition = connection.getHeaderField("Content-Disposition")
    return contentDisposition?.let {
        it.contains("attachment") && it.contains("filename=") && it.contains(".jar")
    } ?: false
}