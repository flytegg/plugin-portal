package gg.flyte.pluginPortal.command.downloadable

import gg.flyte.pluginPortal.type.language.Message.toComponent
import org.bukkit.Bukkit
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class DeleteSubCommand {

    @Subcommand("delete")
    @CommandPermission("pluginportal.delete")
    fun deleteSubCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

}