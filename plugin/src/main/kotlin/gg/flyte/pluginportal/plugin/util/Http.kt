package gg.flyte.pluginportal.plugin.util

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.chat.*
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.security.MessageDigest

fun Plugin.download(marketplacePlatform: MarketplacePlatform, targetDirectory: String, audience: Audience? = null): Boolean {

    val jarFile = File(targetDirectory, "[PP] $name ($marketplacePlatform).jar")

    val file = download(
        URL(platforms[marketplacePlatform]!!.download?.url),
        jarFile,
        audience
    ) ?: return false

    LocalPluginCache.removeIf { plugin -> plugin.id == id }

    LocalPluginCache.add(
        LocalPlugin(
            id = id,
            name = name,
            platform = marketplacePlatform,
            sha256 = calculateSHA256(file),
            sha512 = calculateSHA512(file),
            installedAt = System.currentTimeMillis(),
        )
    )

    LocalPluginCache.save()

    return true
}

fun download(url: URL, file: File, audience: Audience?): File? {
    val connection = url.openConnection()
    connection.setRequestProperty("User-Agent", "Mozilla/5.0")
    connection.connect()
    val inputStream = connection.runCatching {
        getInputStream()
    }.onFailure {
        it.printStackTrace()
        audience?.sendMessage(text("\n").append(
            status(Status.FAILURE, "An error occurred while downloading\n")
                .append(textSecondary("- Please try again, or join our ")
                    .append(SharedComponents.DISCORD_COMPONENT)
                    .appendSecondary(" for support.")
                ).append(endLine()))
        )
        return null
    }.getOrNull()!! // Returns above if failure.

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

fun calculateSHA1(file: File): String {
    val digest = MessageDigest.getInstance("SHA-1")
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

fun calculateSHA512(file: File): String {
    val digest = MessageDigest.getInstance("SHA-512")
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