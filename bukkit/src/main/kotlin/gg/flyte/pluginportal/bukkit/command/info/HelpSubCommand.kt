package gg.flyte.pluginportal.bukkit.command.info

import gg.flyte.pluginportal.bukkit.manager.language.Message.solidLine
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor

@Command("pp", "pluginportal", "ppm", "pportal")
class HelpSubCommand {

    @DefaultFor("~", "~ help")
    fun onHelpCommand(
        sender: Audience
    ) {
        val messages = hashSetOf(
            HelpMessage(
                "Install",
                "pluginportal.install",
                listOf("i"),
                "Install a plugin",
                "/pp install <plugin>"
            ),
            HelpMessage(
                "Update",
                "pluginportal.update",
                listOf("u"),
                "Update an installed plugin",
                "/pp update <plugin>"
            ),
            HelpMessage(
                "Info",
                "pluginportal.info",
                listOf(),
                "Get information about a plugin",
                "/pp info <plugin>"
            ),
            HelpMessage(
                "List",
                "pluginportal.list",
                listOf("ls"),
                "List installed plugins",
                "/pp list"
            ),
            HelpMessage(
                "Menu",
                "pluginportal.menu",
                listOf("m"),
                "Open the Plugin Portal menu",
                "/pp menu"
            )
        )

        val strike = Component.text("")
            .solidLine()
            .color(NamedTextColor.DARK_GRAY)

        sender.sendMessage(
            Component.text().append(
                strike,
                Component.newline(),
                Component.text().append(
                    messages.map { message ->
                        Component.text().append(
                            Component.text(" - ", NamedTextColor.GRAY),
                            Component.text(message.usage, NamedTextColor.AQUA),
                            Component.text(" ( ${message.aliases.joinToString(" ")} ) ", NamedTextColor.DARK_GRAY),
                            Component.text(" | ", NamedTextColor.GRAY),
                            Component.text(message.description, NamedTextColor.GRAY),
                            Component.newline(),
                        ).build()
                    },
                ).build(),
                strike
            )
        )

    }

    private data class HelpMessage(
        val message: String,
        val permission: String,
        val aliases: List<String>,
        val description: String,
        val usage: String
    )
}

