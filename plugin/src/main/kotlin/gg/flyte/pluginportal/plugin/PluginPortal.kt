package gg.flyte.pluginportal.plugin

import com.google.gson.GsonBuilder
import gg.flyte.pluginportal.plugin.command.CommandManager
import gg.flyte.pluginportal.plugin.command.HelpSubCommand
import gg.flyte.pluginportal.plugin.command.ViewSubCommand
import io.papermc.lib.PaperLib
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitCommandHandler

class PluginPortal : JavaPlugin() {

    companion object {
        lateinit var instance: JavaPlugin
    }

    override fun onEnable() {
        instance = this

        CommandManager

        PaperLib.suggestPaper(this)
    }

}