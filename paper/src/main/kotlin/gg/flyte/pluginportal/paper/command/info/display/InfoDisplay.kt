package gg.flyte.pluginportal.paper.command.info.display

import gg.flyte.pluginportal.api.type.MarketplacePlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import java.math.RoundingMode

object InfoDisplay {

    class DefaultDisplay : InfoInterface {
        val SOLID_LINE =
            "<u><dark_gray>                                                                               </dark_gray></u>"

        override fun getDisplayInfo(plugin: MarketplacePlugin): Component {

            return MiniMessage.miniMessage().deserialize(
                """
                    $SOLID_LINE
                    <gray>┌ </gray><aqua>${plugin.getUniqueName()}</aqua>
                    <gray>├─ </gray><aqua>${plugin.statistics.downloads}</aqua> <aqua><u><bold>⬇</bold></u></aqua> <gray>| </gray><aqua>${plugin.statistics.ratingAverage.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()}</aqua> <aqua><u><bold>⭐</bold></u></aqua> <gray>| </gray><aqua>${if ((plugin.statistics.price ?: 0) > 0) "\$${plugin.statistics.price}" else "FREE"}</aqua>
                    <gray>├─ </gray><gray>${plugin.displayInfo.description}</gray>
                    
                    <click:suggest_command:'/ppm install ${plugin.id} -isId true'><green><b><u>INSTALL</u></b></green></click>
                    
                    $SOLID_LINE
                """.trimIndent()
            )


//            return Component.text().append(
//                Component.empty()
//                    .solidLine()
//                    .color(NamedTextColor.DARK_GRAY),
//
//                Component.newline(),
//                Component.text("┌ ", NamedTextColor.GRAY),
//                displayInformation(plugin.getUniqueName())
//                    .color(NamedTextColor.AQUA)
//                    .decorate(TextDecoration.BOLD),
//                Component.newline(),
//                Component.text("├─ ", NamedTextColor.GRAY),
//                Component.text(plugin.statistics.downloads, NamedTextColor.AQUA),
//                Component.space(),
//                Component.text("⬇", NamedTextColor.AQUA, TextDecoration.UNDERLINED, TextDecoration.BOLD),
//                Component.text(" | ", NamedTextColor.GRAY),
//                Component.text(
//                    plugin.statistics.ratingAverage.toBigDecimal().setScale(
//                        1,
//                        RoundingMode.UP
//                    ).toDouble(),
//                    NamedTextColor.AQUA
//                ),
//
//                Component.text(" ⭐", NamedTextColor.GOLD, TextDecoration.BOLD),
//                Component.text(" | ", NamedTextColor.GRAY),
//                Component.text(
//                    if ((plugin.statistics.price ?: 0) > 0) "\$${plugin.statistics.price}" else "FREE",
//                    NamedTextColor.AQUA
//                ),
//
//                Component.newline(),
//                createDescriptionLines(plugin.displayInfo.description),
//                Component.newline(),
//                Component.empty()
//                    .solidLine()
//                    .color(NamedTextColor.DARK_GRAY)
//
//            ).build()
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