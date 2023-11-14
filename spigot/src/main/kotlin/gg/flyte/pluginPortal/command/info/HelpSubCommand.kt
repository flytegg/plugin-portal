package gg.flyte.pluginPortal.command.info

import gg.flyte.pluginPortal.type.extension.sendInfo
import gg.flyte.pluginPortal.type.manager.language.Message.toComponent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
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
            with(sender) {
                val strike = Component.text("")
                    .color(NamedTextColor.GRAY)
                    .solidLine()

                sendMessage(strike)
                messages.forEach { sendMessage(it.toComponent()) }
                sendMessage(strike)
            }

        }
    }

    private data class HelpMessage(val message: String, val permission: String, val aliases: List<String>, val description: String, val usage: String) {
        fun toComponent() = message.toComponent()
    }

    fun Component.solidLine(): TextComponent {
        return Component.text("                                                                               ").decorate(
            TextDecoration.STRIKETHROUGH)
    }
}

