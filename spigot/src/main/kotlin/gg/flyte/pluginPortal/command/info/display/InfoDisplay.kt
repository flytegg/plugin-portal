package gg.flyte.pluginPortal.command.info.display

import gg.flyte.common.api.plugins.schemas.MarketplacePlugin
import gg.flyte.pluginPortal.type.manager.language.Message.serialize
import gg.flyte.pluginPortal.type.manager.language.Message.toComponent
import gg.flyte.twilight.extension.solidLine
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import java.math.RoundingMode

object InfoDisplay {

    class DefaultDisplay : InfoInterface {
        override fun getDisplayInfo(plugin: MarketplacePlugin): Component {

            return Component.empty()
                .append(
                    Component.empty()
                        .solidLine()
                        .color(NamedTextColor.DARK_GRAY)
                )
                .appendNewline()
                .append(Component.text("┌ ", NamedTextColor.GRAY))
                .append(Component.text("Name: ", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(displayInformation(plugin.getUniqueName()))
                .appendNewline()
                .append(Component.text("├─ ", NamedTextColor.GRAY))
                .append(Component.text(plugin.stats.downloads, NamedTextColor.AQUA))
                .appendSpace()
                .append(Component.text("⬇", NamedTextColor.AQUA, TextDecoration.UNDERLINED, TextDecoration.BOLD))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(
                    Component.text(
                        plugin.stats.ratingAverage.toBigDecimal().setScale(
                            1,
                            RoundingMode.UP
                        ).toDouble(),
                        NamedTextColor.AQUA
                    )
                )
                .append(Component.text(" ⭐", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(
                    Component.text(
                        if ((plugin.stats.price ?: 0) > 0) "\$${plugin.stats.price}" else "FREE",
                        NamedTextColor.AQUA
                    )
                )
                .appendNewline()
                .append(createDescriptionLines(plugin.displayInfo.description))
                .appendNewline()
                .append(
                    Component.empty()
                        .solidLine()
                        .color(NamedTextColor.DARK_GRAY)
                )
        }
    }

    fun createDescriptionLines(description: String): TextComponent {
        val descriptionLines = description.chunked(40)

        return Component.text().append(
            descriptionLines.map { string ->
                Component.text("│ $string", NamedTextColor.GRAY)
                    .append(Component.newline())
            }
        ).build()
    }

    /**
     * Creates a TextComponent with the given string and colors it and truncates it if it is too long with a hover event.
     *
     * @param text The string to create the TextComponent from.
     * @return The TextComponent
     */
    fun displayInformation(text: String, length: Int = 40): Component {
        if (text.length < length + 5) {
            return Component.text(text)
                .color(NamedTextColor.GRAY)

        }

        return Component.text(text.substring(0, length))
            .append(
                Component.text(" [...]")
                    .color(NamedTextColor.GRAY)
//                    .hoverEvent(HoverEvent.showText(Component.text(text).color(NamedTextColor.GRAY)))
            )
    }
}