package gg.flyte.pluginPortal.command

import gg.flyte.pluginPortal.type.manager.language.Message.toComponent
import org.bukkit.Bukkit
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission


@Command("pp", "pluginportal", "ppm", "pportal")
class PPCommand  {

    @Subcommand("preview", "p")
    @CommandPermission("pluginportal.preview")
    fun onPreviewCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

    @Subcommand("list", "ls")
    @CommandPermission("pluginportal.list")
    fun onListCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

    @Subcommand("info")
    @CommandPermission("pluginportal.info")
    fun onInfoCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

    @Subcommand("search", "s")
    @CommandPermission("pluginportal.search")
    fun onSearchCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

    @Subcommand("request")
    @CommandPermission("pluginportal.request")
    fun onRequestCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

}