package gg.flyte.pluginPortal.command.info

import gg.flyte.twilight.extension.solidLine
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

