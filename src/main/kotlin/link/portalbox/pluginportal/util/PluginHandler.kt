package link.portalbox.pluginportal.util

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.file.LocalPlugin
import link.portalbox.pplib.type.MarketplacePlugin
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL

fun delete(pluginPortal: PluginPortal, localPlugin: LocalPlugin): Boolean {
    for (loadedPlugin in pluginPortal.server.pluginManager.plugins) {
        val pluginClass = loadedPlugin.javaClass
        val codeSource = pluginClass.protectionDomain.codeSource
        if (codeSource != null) {
            val file = File(codeSource.location.toURI().path)
            if (localPlugin.fileSha == getSha(file)) {
                pluginPortal.server.pluginManager.disablePlugin(loadedPlugin)
                file.delete()
                Data.delete(localPlugin.id)
                return true
            }
        }
    }
    return false
}

fun install(marketplacePlugin: MarketplacePlugin, downloadURL: URL) {
    val outputFile = File("plugins", "${marketplacePlugin.spigotName}-${marketplacePlugin.version} (PP).jar")
    FileUtils.copyURLToFile(downloadURL, outputFile)
    Data.update(marketplacePlugin.id, marketplacePlugin.version, getSha(outputFile))
}