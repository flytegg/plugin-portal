package gg.flyte.pluginPortal.type.manager

import gg.flyte.common.api.PPPluginCache
import gg.flyte.common.api.interfaces.InstalledPluginLoader
import gg.flyte.common.type.api.plugin.InstalledPlugin
import gg.flyte.common.util.GSON
import gg.flyte.common.util.toJson
import gg.flyte.pluginPortal.PluginPortal
import org.apache.commons.lang.mutable.Mutable
import org.bukkit.event.player.PlayerInteractEvent
import java.io.File

object SpigotInstalledPluginLoader : InstalledPluginLoader {
    override val configFile: File = File(PluginPortal.instance.dataFolder, "plugins.json")

    override fun addInstalledPlugin(plugin: InstalledPlugin) {
        println(plugin.toJson())
        PPPluginCache.addInstalledPlugins(plugin)
    }

    override fun loadInstalledPlugins() {
        GSON.fromJson(
            configFile.readText(),
            Array<InstalledPlugin>::class.java
        ).forEach { PPPluginCache.addInstalledPlugins(it) }
    }

    override fun saveInstalledPlugins() {
        configFile.writeText(GSON.toJson(PPPluginCache.getInstalledPlugins()))
    }
}