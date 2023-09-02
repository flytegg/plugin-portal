package gg.flyte.pluginPortal.command.downloadable

import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class RequestSubCommand {

    @Subcommand("request")
    @CommandPermission("pluginportal.request")
    @AutoComplete("@marketplacePlugin")
    fun requestSubCommand(sender: Audience, plugin: String) {

    }

}