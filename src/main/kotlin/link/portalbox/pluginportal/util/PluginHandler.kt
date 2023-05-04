package link.portalbox.pluginportal.util

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.type.Config
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.type.LocalPlugin
import link.portalbox.pplib.type.MarketplacePlugin
import link.portalbox.pplib.type.MarketplaceService
import link.portalbox.pplib.util.download
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.UnknownDependencyException
import java.io.File
import java.net.URL


fun delete(pluginPortal: PluginPortal, localPlugin: LocalPlugin): Boolean {
    for (loadedPlugin in pluginPortal.server.pluginManager.plugins) {
        val pluginClass = loadedPlugin.javaClass
        val codeSource = pluginClass.protectionDomain.codeSource
        if (codeSource != null) {
            runCatching {
                val file = File(codeSource.location.toURI().path)
                if (localPlugin.fileSha == getSHA(file)) {
                    pluginPortal.server.pluginManager.disablePlugin(loadedPlugin)
                    file.delete()
                    Data.delete(localPlugin.id)
                    return true
                }
            }
        }
    }
    return false
}

fun install(plugin: MarketplacePlugin, downloadURL: URL) {
    install(plugin, downloadURL, false)
}

fun install(plugin: MarketplacePlugin, downloadURL: URL, enable: Boolean) {
    val outputFile = File("plugins", "${plugin.name}-${plugin.version} (PP).jar")
    download(downloadURL, outputFile)
    // find the plugins name using id
    // extract author and slug from download url if service == service.HANGAR
    var id = "${plugin.service}:${plugin.id}"
    if (plugin.service == MarketplaceService.HANGAR) {
        val url = plugin.downloadURL
        val author = url.split("/")[6]
        val slug = url.split("/")[7]

        id = "${plugin.service}:${author}:${slug}"
    }

    Data.update(id, plugin.version, getSHA(outputFile))
    addValueToPieChart(Chart.MOST_DOWNLOADED, plugin.id)
    if (enable || Config.startupOnInstall) {
        enablePlugin(Bukkit.getPluginManager().loadPlugin(outputFile))
    }
}

fun enablePlugin(plugin: Plugin?) {
    if (plugin == null) {
        return
    }
    try {
        Bukkit.getPluginManager().enablePlugin(plugin)
        plugin.onEnable()
    } catch (e: NoSuchMethodError) {
        e.printStackTrace()
    } catch (e: UnknownDependencyException) {
        e.printStackTrace()
    }
}