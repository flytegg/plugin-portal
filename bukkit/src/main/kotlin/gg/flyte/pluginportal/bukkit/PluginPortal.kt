package gg.flyte.pluginportal.bukkit

import gg.flyte.pluginportal.bukkit.command.CloudCommandManager
import gg.flyte.pluginportal.bukkit.command.CommandManager
import gg.flyte.pluginportal.bukkit.manager.Config
import gg.flyte.pluginportal.bukkit.manager.PPPluginCache
import gg.flyte.pluginportal.bukkit.manager.PluginManager
import gg.flyte.twilight.Twilight
import gg.flyte.twilight.event.custom.interact.listener.InteractEventListener
import gg.flyte.twilight.event.disableCustomEventListeners
import gg.flyte.twilight.event.event
import gg.flyte.twilight.twilight
import io.papermc.lib.PaperLib
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bstats.bukkit.Metrics
import org.bukkit.command.CommandSender
import org.bukkit.event.server.TabCompleteEvent
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager

class PluginPortal : JavaPlugin() {

    companion object {
        val instance by lazy { Twilight.plugin as PluginPortal }
        val api = PluginManager

        val isPremium = false
    }

    override fun onEnable() {
        twilight(this) {
            disableCustomEventListeners(InteractEventListener)
        }

        Config.init(this)
//        CommandManager

        Metrics(this, 18005)
        PaperLib.suggestPaper(this)

        CloudCommandManager

        asyncDispatch {
            PPPluginCache.loadInstalledPlugins()
        }
    }

    override fun onDisable() {
        PPPluginCache.saveInstalledPlugins()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun asyncDispatch(block: suspend () -> Unit) = GlobalScope.launch { block.invoke() }
}