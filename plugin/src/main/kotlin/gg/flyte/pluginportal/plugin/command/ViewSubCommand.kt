package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.*

@Command("pp", "pluginportal", "ppm")
class ViewSubCommand {

    @Subcommand("view")
    @AutoComplete("@marketplacePluginSearch *")
    fun viewCommand(
        audience: Audience,
        @Optional prefix: String? = null,
        @Optional @Flag("platform") platformFlag: MarketplacePlatform? = null,
        @Optional @Flag("id") idFlag: String? = null,
    ) {
        if (prefix == null) {
            if (idFlag == null) {
                return audience.sendMessage(
                    status(Status.FAILURE, "No plugin name or ID provided").boxed()
                )
            } else {
                audience.sendMessage(text("Command not implemented yet", NamedTextColor.RED))
            }
        }

        val plugins = API.getPlugins(prefix).ifEmpty {
            return audience.sendMessage(status(Status.FAILURE, "No plugins found").boxed())
        }

        if (plugins.size == 1) return audience.sendMessage(plugins.first().getImageComponent().boxed())

        audience.sendMessage(
            startLine()
                .appendSecondary("Multiple plugins found, click one to view more information")
                .appendNewline()
        )

        plugins.forEach { plugin ->
            audience.sendMessage(
                textSecondary(" - ")
                    .appendPrimary(plugin.name)
                    .append(
                        textSecondary(" (")
                            .appendDark(plugin.platforms.keys.joinToString(", "))
                            .appendDark(")")
                    )
                    .hoverEvent(text("Click to view more information"))
                    .suggestCommand("/pp view ${plugin.name}")
            )
        }

        audience.sendMessage(endLine())
    }
}

fun Plugin.getImageComponent(): Component {
    val description: List<String> = splitDescriptionIntoLines(getDescription() ?: "", 35)

    val image = ChatImage.ImageTextBuilder(getImageURL() ?: "")
        .setLine(0, textPrimary(name).bold())
        .apply {
            description.forEachIndexed { index, line -> setLine(index + 2, textSecondary(line)) }
        }
        .setLine(description.size + 3, textSecondary("Downloads: ${getDownloads().format()}"))
        .setLine(description.size + 4, textSecondary("Platforms: ${platforms.keys.joinToString()}"))
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
