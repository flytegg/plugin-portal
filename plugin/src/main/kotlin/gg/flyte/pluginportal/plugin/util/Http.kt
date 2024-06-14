package gg.flyte.pluginportal.plugin.util

import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import java.io.File
import java.net.URL

fun Plugin.download(marketplacePlatform: MarketplacePlatform) {
    download(URL(platforms[marketplacePlatform]!!.download?.url), "[PP] $name ($marketplacePlatform).jar")
}

fun download(url: URL, name: String) {
    val connection = url.openConnection()
    connection.setRequestProperty("User-Agent", "Mozilla/5.0")
    connection.connect()
    val inputStream = connection.getInputStream()
    val file = File("plugins/${name}")
    file.parentFile.mkdirs()
    file.createNewFile()
    file.outputStream().use { outputStream ->
        inputStream.copyTo(outputStream)
    }
    inputStream.close()

    println("Downloaded ${url.file}")
}