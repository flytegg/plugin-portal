package gg.flyte.pluginportal.plugin

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.plugin.command.CommandManager
import gg.flyte.pluginportal.plugin.config.Config
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import io.papermc.lib.PaperLib
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class PluginPortal : JavaPlugin() {

    companion object {
        lateinit var instance: JavaPlugin
        lateinit var pluginPortalJarFile: File
    }


    override fun onEnable() {
        instance = this
        pluginPortalJarFile = this.file

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