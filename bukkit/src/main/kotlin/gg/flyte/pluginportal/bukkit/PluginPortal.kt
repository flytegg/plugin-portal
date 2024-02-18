package gg.flyte.pluginportal.bukkit

import gg.flyte.pluginportal.bukkit.command.CommandManager
import gg.flyte.pluginportal.bukkit.manager.Config
import gg.flyte.pluginportal.bukkit.manager.PPPluginCache
import gg.flyte.pluginportal.bukkit.manager.PluginManager
import gg.flyte.twilight.Twilight
import gg.flyte.twilight.data.sql.QueryBuilder.Companion.eq
import gg.flyte.twilight.event.event
import gg.flyte.twilight.twilight
import io.papermc.lib.PaperLib
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bstats.bukkit.Metrics
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChatTabCompleteEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerCommandSendEvent
import org.bukkit.event.server.TabCompleteEvent
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.ktx.commandError

class PluginPortal : JavaPlugin() {

    companion object {
        val instance by lazy { Twilight.plugin as PluginPortal }
        val api = PluginManager

        val isPremium = false
    }

    override fun onEnable() {
        twilight(this)

        Config.init(this)
        asyncDispatch { PPPluginCache.loadInstalledPlugins() }

        CommandManager

        Metrics(this, 18005)
        PaperLib.suggestPaper(this)
    }

    override fun onDisable() {
        PPPluginCache.saveInstalledPlugins()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun asyncDispatch(block: suspend () -> Unit) = GlobalScope.launch { block.invoke() }
}