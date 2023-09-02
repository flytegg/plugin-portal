package gg.flyte.pluginPortal.command.toggle

import gg.flyte.pluginPortal.type.language.Message.toComponent
import org.bukkit.Bukkit
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class DisableSubCommand {

    @Subcommand("disable")
    @CommandPermission("pluginportal.disable")
    fun disableSubCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }
}
