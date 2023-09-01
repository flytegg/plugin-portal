package gg.flyte.pluginPortal.command

import gg.flyte.pluginPortal.type.language.Message.toComponent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Default
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission
import revxrsal.commands.orphan.OrphanCommand

@Command("pp", "pluginportal", "ppm")
class PPCommand  {

    @DefaultFor("pp", "pluginportal", "ppm")
    fun onPPCommand(sender: Audience) {
        Bukkit.broadcast("Hello, help!".toComponent())
    }

    @Subcommand("help")
    fun onHelpCommand() {
        Bukkit.broadcast("Hello, world!".toComponent())
    }

    @Subcommand("install", "i")
    @CommandPermission("pluginportal.install")
    fun onInstallCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

}