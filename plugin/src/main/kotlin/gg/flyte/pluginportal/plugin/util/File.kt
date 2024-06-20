package gg.flyte.pluginportal.plugin.util

import java.net.HttpURLConnection
import java.net.URL

fun isValidDownload(url: String): Boolean {
    return isJarDownloadUrl(url)
}

fun isJarDownloadUrl(url: String): Boolean {
    if (url.endsWith(".jar")) return true

    // Support horrible SpigotMC download URLs
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.instanceFollowRedirects = false
    val contentDisposition = connection.getHeaderField("Content-Disposition")
    return contentDisposition?.let {
        it.contains("attachment") && it.contains("filename=") && it.contains(".jar")
    } ?: false
}