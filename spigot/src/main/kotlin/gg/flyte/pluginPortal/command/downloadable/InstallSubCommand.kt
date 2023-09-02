package gg.flyte.pluginPortal.command.downloadable

import gg.flyte.pluginPortal.type.annotation.PPPlugin
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class InstallSubCommand {

    @Subcommand("install", "i")
    @CommandPermission("pluginportal.install")
    @AutoComplete("@ppPlugin")
    fun installSubCommand(sender: Audience, @PPPlugin pluginName: String) {

    }

}