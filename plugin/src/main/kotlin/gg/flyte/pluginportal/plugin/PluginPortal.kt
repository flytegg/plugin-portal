package gg.flyte.pluginportal.plugin

import gg.flyte.pluginportal.plugin.command.PluginPortalCommand
import io.papermc.lib.PaperLib
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitCommandHandler

class PluginPortal : JavaPlugin() {

    companion object {
        lateinit var instance: JavaPlugin
    }

    override fun onEnable() {
        instance = this

        BukkitCommandHandler.create(this).apply {
            enableAdventure()
            register(PluginPortalCommand())
        }

        PaperLib.suggestPaper(this)
    }

}