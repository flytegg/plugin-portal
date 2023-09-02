package gg.flyte.pluginPortal.command.downloadable

import gg.flyte.pluginPortal.type.language.Message.toComponent
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class RequestSubCommand {

    @Subcommand("request")
    @CommandPermission("pluginportal.request")
    fun requestSubCommand(sender: Audience) {
        Bukkit.broadcast()
    }

}