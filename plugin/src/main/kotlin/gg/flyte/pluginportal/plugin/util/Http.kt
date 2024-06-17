package gg.flyte.pluginportal.plugin.util

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.security.MessageDigest

fun Plugin.download(marketplacePlatform: MarketplacePlatform) {
    val file = download(URL(platforms[marketplacePlatform]!!.download?.url), "[PP] $name ($marketplacePlatform).jar")

    val sha256 = calculateSHA256(file)

    LocalPluginCache.add(LocalPlugin(
        id = id,
        name = name,
        platform = marketplacePlatform,
        sha256 = sha256,
        installedAt = System.currentTimeMillis(),
    ))

    LocalPluginCache.save()
}

fun download(url: URL, name: String): File {
    val connection = url.openConnection()
    connection.setRequestProperty("User-Agent", "Mozilla/5.0")
    connection.connect()
    val inputStream = connection.getInputStream()
    val file = File("plugins/$name")
    file.parentFile.mkdirs()
    file.createNewFile()
    file.outputStream().use { outputStream ->
        inputStream.copyTo(outputStream)
    }
    inputStream.close()
    return file
}

fun calculateSHA256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    FileInputStream(file).use { fis ->
        val byteArray = ByteArray(1024)
        var bytesCount: Int

        while (fis.read(byteArray).also { bytesCount = it } != -1) {
            digest.update(byteArray, 0, bytesCount)
        }
    }
    val bytes = digest.digest()
    val sb = StringBuilder()
    for (byte in bytes) {
        sb.append(String.format("%02x", byte))
    }
    return sb.toString()
}
