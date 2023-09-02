package gg.flyte.pluginPortal.command.info

import gg.flyte.pluginPortal.type.language.Message.toComponent
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Default
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.SecretCommand
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.help.CommandHelp

@Command("pp", "pluginportal", "ppm", "pportal")
class HelpSubCommand {

    @Subcommand("help")
    fun onHelpCommand(sender: Audience) {
        arrayListOf(
            HelpMessage(
                "Install",
                "pluginportal.install",
                listOf("i"),
                "Install a plugin from the marketplace",
                "/pp install <plugin>"
            ),
            HelpMessage(
                "Uninstall",
                "pluginportal.uninstall",
                listOf("u"),
                "Uninstall a plugin from the marketplace",
                "/pp uninstall <plugin>"
            ),
        )
    }

}

data class HelpMessage(val message: String, val permission: String, val aliases: List<String>, val description: String, val usage: String) {
    fun toComponent() = message.toComponent()
}