package gg.flyte.pluginportal.bukkit

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import gg.flyte.pluginportal.api.PluginPortalAPI
import gg.flyte.pluginportal.bukkit.command.CommandManager
import gg.flyte.pluginportal.bukkit.manager.Config
import gg.flyte.pluginportal.bukkit.manager.PPPluginCache
import gg.flyte.pluginportal.bukkit.manager.PluginManager
import gg.flyte.twilight.twilight
import io.papermc.lib.PaperLib
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class PluginPortal : SuspendingJavaPlugin() {

    companion object {
        lateinit var instance: PluginPortal
        val api = PluginManager
    }

    override fun onEnable() {
        instance = this

        twilight(this)
        Config.init(this)
        CommandManager

        PPPluginCache.loadInstalledPlugins()

        Metrics(this, 18005)
        PaperLib.suggestPaper(this)

    }

    override fun onDisable() {
        PPPluginCache.saveInstalledPlugins()
    }

    inline fun asyncDispatch(crossinline block: suspend () -> Unit) = launch { withContext(asyncDispatcher) { block() } }
}