package gg.flyte.pluginPortal.command.downloadable

import gg.flyte.pluginPortal.type.manager.language.Message.toComponent
import org.bukkit.Bukkit
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class UpdateSubCommand {

    @Subcommand("update")
    @CommandPermission("pluginportal.update")
    fun updateSubCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

}