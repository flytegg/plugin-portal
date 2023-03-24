package link.portalbox.pluginportal.util

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.file.LocalPlugin
import link.portalbox.pplib.type.SpigetPlugin
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileNotFoundException
import java.net.URL

fun delete(pluginPortal: PluginPortal, localPlugin: LocalPlugin): Boolean {
    for (loadedPlugin in pluginPortal.server.pluginManager.plugins) {
        val pluginClass = loadedPlugin.javaClass
        val codeSource = pluginClass.protectionDomain.codeSource
        if (codeSource != null) {
            try {
                val file = File(codeSource.location.toURI().path)
                if (localPlugin.fileSha == getSha(file)) {
                    pluginPortal.server.pluginManager.disablePlugin(loadedPlugin)
                    file.delete()
                    Data.delete(localPlugin.id)
                    return true
                }
            } catch (x: FileNotFoundException) {
                continue
            }
        }
    }
    return false
}

fun install(spigetPlugin: SpigetPlugin, downloadURL: URL) {
    val outputFile = File("plugins", "${spigetPlugin.name}-${spigetPlugin.onlineVersion} (PP).jar")
    FileUtils.copyURLToFile(downloadURL, outputFile)
    Data.update(spigetPlugin.id.toInt(), spigetPlugin.onlineVersion, getSha(outputFile))
}