package gg.flyte.pluginportal.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import gg.flyte.pluginportal.paper.command.CommandManager
import gg.flyte.pluginportal.paper.config.Config
import gg.flyte.pluginportal.paper.plugin.PPPluginCache
import gg.flyte.twilight.Twilight
import gg.flyte.twilight.twilight
import io.papermc.lib.PaperLib
import io.papermc.lib.PaperLib.isSpigot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bstats.bukkit.Metrics

class PluginPortal : SuspendingJavaPlugin() {

    companion object {
        val instance by lazy { Twilight.plugin as PluginPortal }
        val asyncContext by lazy { if (isFolia()) instance.asyncDispatcher else Dispatchers.IO }

        fun isFolia(): Boolean {
            return try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
                true
            } catch (_: ClassNotFoundException) {
                false
            }
        }
    }

    override fun onEnable() {
        PaperLib.suggestPaper(this)

        if (isSpigot()) {
            for (i in 1..5) {
                logger.warning("PluginPortal is not supported on Spigot. Please download Paper from https://papermc.io.")
            }

            server.pluginManager.disablePlugin(this)
        }
    }

    override suspend fun onEnableAsync() {
        twilight(this)

        Config
        CommandManager
        Metrics(this, 18005)

        withContext(asyncContext) { PPPluginCache.loadInstalledPlugins() }
    }

}