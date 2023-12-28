package gg.flyte.pluginportal

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import gg.flyte.pluginportal.api.PluginPortalAPI
import gg.flyte.pluginportal.command.CommandManager
import gg.flyte.pluginportal.manager.Config
import gg.flyte.pluginportal.manager.PPPluginCache
import gg.flyte.twilight.TwilightKt.twilight
import io.papermc.lib.PaperLib
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class PluginPortal : JavaPlugin(){

    companion object {
        lateinit var instance: PluginPortal
        lateinit var api: PluginPortalAPI
    }

    override fun onEnable() {
        instance = this

        twilight(this) { null }

        Config.init(this)

        CommandManager.init()

        PPPluginCache.loadInstalledPlugins()

        Metrics(this, 18005)
        PaperLib.suggestPaper(this)

    }

    override fun onDisable() { PPPluginCache.saveInstalledPlugins() }

    fun asyncDispatch(block: suspend () -> Unit) = launch { withContext(asyncDispatcher) { block() } }
}