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

    val jarFile = File(targetDirectory, "[PP] $downloadableName ($marketplacePlatform).jar")

    val file = download(
        URL(platforms[marketplacePlatform]!!.download?.url),
        jarFile,
        audience
    ) ?: return false

    LocalPluginCache.removeIf { plugin -> plugin.platformId == id }

    LocalPluginCache.add(
        LocalPlugin(
            platformId = platforms[marketplacePlatform]!!.id,
            name = name,
            version = platforms[marketplacePlatform]?.download!!.version,
            platform = marketplacePlatform,
            sha256 = HashType.SHA256.hash(file),
            sha512 = HashType.SHA512.hash(file),
            installedAt = System.currentTimeMillis(),
        )
    )

    LocalPluginCache.save()

    return true
}

fun download(url: URL, file: File, audience: Audience?): File? = runCatching {
    url.openConnection().apply {
        setRequestProperty("User-Agent", "Mozilla/5.0")
        connect()
    }.getInputStream().use { input ->
        file.parentFile.mkdirs()
        file.outputStream().use { output -> input.copyTo(output) }
    }
    file
}.onFailure {
    it.printStackTrace()
    audience?.sendMessage(
        text("\n").append(
            status(Status.FAILURE, "An error occurred while downloading\n")
                .append(textSecondary("- Please try again, or join our ")
                    .append(SharedComponents.DISCORD_COMPONENT)
                    .appendSecondary(" for support.")
                ).append(endLine())
        )
    )
}.getOrNull()