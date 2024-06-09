package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.plugin.util.ChatImage
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
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
            val plugin = plugins.first()

            val platform = plugin.platforms.entries.first().value

            audience.sendMessage(
                MiniMessage.miniMessage().deserialize(
                    "<gradient:blue:green>${plugin.name}</gradient> <gray>${platform.description}</gray>"
                )
            )
        }

        plugins.forEach { plugin ->
            val imageURL = plugin.platforms.entries.firstNotNullOf { it.value.imageURL }
            val description = plugin.platforms.entries.firstNotNullOf { it.value.description }

            var downloads = 0
            plugin.platforms.entries.forEach {
                downloads += it.value.downloads
            }

            val descriptionLines = splitDescriptionIntoLines(description, 35)

            val builder = ChatImage.ImageTextBuilder(imageURL ?: "")
                .setLine(0, text(plugin.name, NamedTextColor.AQUA, TextDecoration.BOLD))

            descriptionLines.forEachIndexed { index, line ->
                builder.setLine(index + 2, text(line, NamedTextColor.GRAY))
            }

            builder.setLine(descriptionLines.size + 3, text("Downloads: $downloads", NamedTextColor.GRAY))
                .setLine(descriptionLines.size + 4, text("Platforms: ${plugin.platforms.keys.joinToString()}", NamedTextColor.GRAY))
                .build()
                .let { audience.sendMessage(it) }

            audience.sendMessage(
                text(
                    plugin.name + plugin.platforms.keys.joinToString(
                        prefix = " (",
                        postfix = ")",
                        separator = ", "
                    )
                )
            )
        }
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
}
