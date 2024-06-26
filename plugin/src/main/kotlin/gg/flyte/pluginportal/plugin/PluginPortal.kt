package gg.flyte.pluginportal.plugin

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.plugin.command.CommandManager
import gg.flyte.pluginportal.plugin.config.Config
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import io.papermc.lib.PaperLib
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin


class PluginPortal : JavaPlugin() {

    companion object {
        lateinit var instance: JavaPlugin
    }

    override fun onEnable() {
        instance = this

        CommandManager
        Config

        LocalPluginCache.load()

        PaperLib.suggestPaper(this)
        Metrics(this, 18005)

        API // LOAD FOR ONDISABLE
    }

    override fun onDisable() {
        API.closeClient()
    }

}