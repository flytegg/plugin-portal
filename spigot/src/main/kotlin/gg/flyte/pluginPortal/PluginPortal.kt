package gg.flyte.pluginPortal

import gg.flyte.pluginPortal.command.CommandManager
import gg.flyte.pluginPortal.manager.Config
import gg.flyte.pluginPortal.manager.PPPluginCache
import gg.flyte.twilight.twilight
import io.papermc.lib.PaperLib
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class PluginPortal : JavaPlugin() {

    companion object {
        lateinit var instance: PluginPortal
    }

    override fun onEnable() {
        instance = this

        twilight(this) {}
        Config.init(this)

        CommandManager.init()

        PPPluginCache.loadInstalledPlugins()

        Metrics(this, 18005)
        PaperLib.suggestPaper(this)
    }

    override fun onDisable() { PPPluginCache.saveInstalledPlugins() }

}