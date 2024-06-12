package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.util.ChatImage
import gg.flyte.pluginportal.plugin.util.solidLine
import gg.flyte.pluginportal.plugin.util.translate
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand

@Command("pp", "pluginportal", "ppm")
class ViewSubCommand {

    @Subcommand("view")
    @AutoComplete("@marketplacePluginSearch *")
    fun viewCommand(audience: Audience, prefix: String) {
        val plugins = API.getPlugins(prefix)

        if (plugins.isEmpty()) return audience.sendMessage(text("No plugins found"))

        if (plugins.size == 1) {
            audience.sendMessage(
                solidLine()
                    .append(
                        plugins.first()
                            .getImageComponent()
                            .decoration(TextDecoration.STRIKETHROUGH, false)
                    )
                    .append(solidLine(prefix = "\n", suffix = ""))
            )

            return
        }

        audience.sendMessage(
            solidLine()
                .append(
                    text("Multiple plugins found, click one to view more information", NamedTextColor.GRAY)
                        .decoration(TextDecoration.STRIKETHROUGH, false)
                )
                .appendNewline()
        )

        plugins.forEach { plugin ->
            audience.sendMessage(
                text(" - ", NamedTextColor.GRAY)
                    .append(
                        text(plugin.name, NamedTextColor.AQUA)
                            .append(text(" (", NamedTextColor.DARK_GRAY)
                                .append(text(plugin.platforms.keys.joinToString(", "), NamedTextColor.DARK_GRAY))
                                .append(text(")", NamedTextColor.DARK_GRAY))
                            )
                    )
                    .hoverEvent(text("Click to view more information"))
                    .clickEvent(ClickEvent.suggestCommand("/pp view ${plugin.name}"))
            )
        }

        audience.sendMessage(solidLine(prefix = "", suffix = ""))
    }
}

fun Plugin.getImageComponent(): Component {
    val description: List<String> =
        splitDescriptionIntoLines(getDescription() ?: "", 35)

    val image = ChatImage.ImageTextBuilder(getImageURL() ?: "")
        .setLine(0, text(name, NamedTextColor.AQUA, TextDecoration.BOLD))
        .apply {
            description.forEachIndexed { index, line -> setLine(index + 2, text(line, NamedTextColor.GRAY)) }
        }
        .setLine(description.size + 3, text("Downloads: ${getDownloads()}", NamedTextColor.GRAY))
        .setLine(description.size + 4, text("Platforms: ${platforms.keys.joinToString()}", NamedTextColor.GRAY))
        .build()

    return image
}

private fun splitDescriptionIntoLines(description: String, maxLineLength: Int): List<String> {
    val words = description.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = StringBuilder()

    for (word in words) {
        if (currentLine.length + word.length + 1 > maxLineLength) {
            lines.add(currentLine.toString())
            currentLine = StringBuilder()
        }
        if (currentLine.isNotEmpty()) {
            currentLine.append(" ")
        }
        currentLine.append(word)
    }
    if (currentLine.isNotEmpty()) {
        lines.add(currentLine.toString())
    }

    return lines
}