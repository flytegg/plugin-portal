package gg.flyte.pluginPortal.command.info

import gg.flyte.pluginPortal.type.data.HelpMessage
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Subcommand

@Command("pp", "pluginportal", "ppm", "pportal")
class HelpSubCommand {

    @DefaultFor("~", "~ help")
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
                "Delete",
                "pluginportal.delete",
                listOf("d"),
                "Delete a plugin from the server",
                "/pp delete <plugin>"
            ),
        ).let { messages ->
            messages.forEach {
                sender.sendMessage(it.toComponent())
            }
        }
    }
}

