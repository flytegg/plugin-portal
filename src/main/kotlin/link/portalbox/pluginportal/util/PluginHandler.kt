package link.portalbox.pluginportal.util

import gg.flyte.pplib.type.plugin.MarketplacePlugin
import gg.flyte.pplib.util.download
import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.type.Config
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.type.LocalPlugin
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
                    Data.delete(localPlugin.marketplacePlugin.id)
                    return true
                }
            }
        }
    }
    return false
}

fun install(plugin: MarketplacePlugin, pluginPortal: PluginPortal) {
    install(plugin, pluginPortal, false)
}

fun install(plugin: MarketplacePlugin, pluginPortal: PluginPortal, enable: Boolean) {
    Bukkit.getScheduler().runTaskAsynchronously(pluginPortal, Runnable {
        val outputFile = File("plugins", "${plugin.name}-${plugin.version} (PP).jar".replace(":", "~"))
        download(URL(plugin.downloadURL), outputFile)

        Data.update(LocalPlugin(
            getSHA(outputFile),
            plugin,
        ))

        if (enable || Config.startupOnInstall) {
            enablePlugin(Bukkit.getPluginManager().loadPlugin(outputFile))
        }
    })
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