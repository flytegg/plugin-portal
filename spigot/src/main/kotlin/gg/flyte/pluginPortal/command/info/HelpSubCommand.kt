package gg.flyte.pluginPortal.command.info

import gg.flyte.pluginPortal.type.data.HelpMessage
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Subcommand

@Command("pp", "pluginportal", "ppm", "pportal")
class HelpSubCommand {

    @Subcommand("help")
    @DefaultFor("pp", "pp help", "pluginportal", "pluginportal help", "ppm", "ppm help", "pportal", "pportal help")
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
        ).let { messages ->
            messages.forEach {
                sender.sendMessage(it.toComponent())
            }
        }
    }
}

