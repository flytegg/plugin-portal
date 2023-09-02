package gg.flyte.pluginPortal.type.manager

import gg.flyte.common.api.interfaces.InstalledPluginLoader
import gg.flyte.common.type.api.plugin.InstalledPlugin
import gg.flyte.common.util.GSON
import gg.flyte.common.util.toJson
import gg.flyte.pluginPortal.PluginPortal
import org.apache.commons.lang.mutable.Mutable
import org.bukkit.event.player.PlayerInteractEvent
import java.io.File

class SpigotInstalledPluginLoader(val pluginPortal: PluginPortal) : InstalledPluginLoader {
    override val configFile: File = File(pluginPortal.dataFolder, "plugins.json")

    override val installedPlugins: MutableSet<InstalledPlugin> = mutableSetOf()

    override fun addInstalledPlugin(plugin: InstalledPlugin) {
        println(plugin.toJson())
        installedPlugins.add(plugin)
        installedPlugins.forEach { println(it) }
    }

    override fun loadInstalledPlugins() {
        installedPlugins.addAll(GSON.fromJson(
            configFile.readText(),
            HashSet<InstalledPlugin>()::class.java
        ))
    }

    override fun saveInstalledPlugins() {
        configFile.writeText(GSON.toJson(installedPlugins))
    }
}