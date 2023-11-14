package gg.flyte.pluginPortal.command.info

import gg.flyte.pluginPortal.type.manager.language.Message.toComponent
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor

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

    private data class HelpMessage(val message: String, val permission: String, val aliases: List<String>, val description: String, val usage: String) {
        fun toComponent() = message.toComponent()
    }
}

