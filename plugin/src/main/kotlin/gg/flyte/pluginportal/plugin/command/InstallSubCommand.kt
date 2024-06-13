package gg.flyte.pluginportal.plugin.command

import gg.flyte.db.MarketplacePlatform
import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import java.net.URL

@Command("pp", "pluginportal", "ppm")
class InstallSubCommand {

    @Subcommand("install")
    @AutoComplete("@marketplacePluginSearch *")
    fun installCommand(audience: Audience, prefix: String) {
        val plugins = API.getPlugins(prefix)

        if (plugins.isEmpty()) return audience.sendMessage(text("No plugins found"))

        if (plugins.size == 1) {
            val plugin = plugins.first()

            audience.sendMessage(
                solidLine()

                    .append(
                        text("Starting installation of ", NamedTextColor.GRAY)
                            .append(text(plugin.name, NamedTextColor.AQUA))
                            .append(text("...", NamedTextColor.GRAY))
                            .decoration(TextDecoration.STRIKETHROUGH, false)
                    )
                    .append(solidLine(prefix = "\n", suffix = ""))
            )

            plugin.download(plugin.platforms.keys.first())

            audience.sendMessage(text("Downloaded file"))

            return
        }

        audience.sendMessage(
            solidLine()
                .append(
                    text("Multiple plugins found, click one to prompt install command", NamedTextColor.GRAY)
                        .decoration(TextDecoration.STRIKETHROUGH, false)
                )
                .appendNewline()
        )

        plugins.forEach { plugin ->
            audience.sendMessage(
                text(" - ", NamedTextColor.GRAY)
                    .append(
                        text(plugin.name, NamedTextColor.AQUA)
                            .append(
                                text(" (", NamedTextColor.DARK_GRAY)
                                    .append(text(plugin.platforms.keys.joinToString(", "), NamedTextColor.DARK_GRAY))
                                    .append(text(")", NamedTextColor.DARK_GRAY))
                            )
                    )
                    .hoverEvent(text("Click to install"))
                    .clickEvent(ClickEvent.suggestCommand("/pp install ${plugin.name}"))
            )
        }

        audience.sendMessage(solidLine(prefix = "", suffix = ""))
    }
}