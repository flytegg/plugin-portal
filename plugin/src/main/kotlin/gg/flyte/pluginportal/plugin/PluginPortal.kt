package gg.flyte.pluginportal.plugin

import gg.flyte.pluginportal.plugin.command.CommandManager
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import io.papermc.lib.PaperLib
import org.bukkit.plugin.java.JavaPlugin

class PluginPortal : JavaPlugin() {

    companion object {
        lateinit var instance: JavaPlugin
    }

    override fun onEnable() {
        instance = this

        CommandManager

        LocalPluginCache.load()

        PaperLib.suggestPaper(this)
    }

}