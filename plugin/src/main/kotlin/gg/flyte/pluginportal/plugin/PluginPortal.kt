package gg.flyte.pluginportal.plugin

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.plugin.command.CommandManager
import gg.flyte.pluginportal.plugin.config.Config
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.manager.MarketplacePluginCache
import gg.flyte.pluginportal.plugin.util.async
import io.papermc.lib.PaperLib
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class PluginPortal : JavaPlugin() {

    companion object {
        lateinit var instance: PluginPortal
        lateinit var pluginPortalJarFile: File
        val isFreeVersion = true
    }


    override fun onEnable() {
        instance = this
        pluginPortalJarFile = this.file

        Config
        CommandManager

        LocalPluginCache.load()
        async { MarketplacePluginCache.loadLocalPluginData() }

        PaperLib.suggestPaper(this)
        Metrics(this, 18005)

        API // LOAD FOR ONDISABLE
    }

    override fun onDisable() {
        API.closeClient()
    }

}