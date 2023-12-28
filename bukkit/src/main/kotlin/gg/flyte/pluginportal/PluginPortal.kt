package gg.flyte.pluginportal

import gg.flyte.pluginportal.command.CommandManager
import gg.flyte.pluginportal.manager.Config
import gg.flyte.pluginportal.manager.PPPluginCache
import gg.flyte.twilight.TwilightKt.twilight
import io.papermc.lib.PaperLib
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class PluginPortal : JavaPlugin() {

    companion object {
        lateinit var instance: PluginPortal
    }

    override fun onEnable() {
        instance = this

        twilight(this) {
            null
        }
        Config.init(this)

        CommandManager.init()

        PPPluginCache.loadInstalledPlugins()

        Metrics(this, 18005)
        PaperLib.suggestPaper(this)
    }

    override fun onDisable() { PPPluginCache.saveInstalledPlugins() }

}