package gg.flyte.pluginportal.bukkit

import gg.flyte.pluginportal.bukkit.command.CommandManager
import gg.flyte.pluginportal.bukkit.manager.Config
import gg.flyte.pluginportal.bukkit.manager.PPPluginCache
import gg.flyte.pluginportal.bukkit.manager.PluginManager
import gg.flyte.pluginportal.client.PPClient
import gg.flyte.twilight.Twilight
import gg.flyte.twilight.twilight
import gg.flyte.twilight.scheduler.async
import io.papermc.lib.PaperLib
import kotlinx.coroutines.runBlocking
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class PluginPortal : JavaPlugin() {

    companion object {
        val instance by lazy { Twilight.plugin as PluginPortal }
        val api = PluginManager
    }

    override fun onEnable() {
        twilight(this)
        Config.init(this)
        CommandManager

        Metrics(this, 18005)
        PaperLib.suggestPaper(this)

        asyncDispatch {
            PPPluginCache.loadInstalledPlugins()
        }
    }

    override fun onDisable() {
        PPPluginCache.saveInstalledPlugins()
    }

    //    inline fun asyncDispatch(crossinline block: suspend () -> Unit) = launch { runBlocking { block.invoke() } }
    fun asyncDispatch(block: suspend () -> Unit) = async { runBlocking { block.invoke() } }
}