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
import revxrsal.commands.annotation.Flag
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand

@Command("pp", "pluginportal", "ppm")
class InstallSubCommand {

    @Subcommand("install")
    @AutoComplete("@marketplacePluginSearch *")
    fun installCommand(
        audience: Audience,
        @Optional prefix: String? = null,
        @Optional @Flag("platform") platformFlag: MarketplacePlatform? = null,
        @Optional @Flag("id") idFlag: String? = null,
    ) {
        if (prefix == null) {
            if (idFlag == null) {
                return audience.sendMessage(
                    status(Status.FAILURE, "No plugin name or ID provided")
                        .boxed()
                )
            } else {
                audience.sendMessage(text("Command not implemented yet", NamedTextColor.RED))
            }
        }

        val plugins = API.getPlugins(prefix)

        if (plugins.isEmpty()) {
            return audience.sendMessage(
                status(Status.FAILURE, "No plugins found")
                    .boxed()
            )
        }

        if (plugins.size == 1) {
            val plugin = plugins.first()
            val platforms = plugin.platforms

            audience.sendMessage(
                startLine()
                    .appendSecondary("Starting installation of ")
                    .appendPrimary(plugin.name)
                    .appendSecondary("...")
            )

            if (platforms.size == 1 || platformFlag != null) {
                val entry = platforms.entries
                    .first()

                val platform = platformFlag ?: entry.key
                val platformPlugin = platforms[platformFlag] ?: entry.value

                if (platformPlugin.download?.url == null) {
                    return audience.sendMessage(
                        text("No download URL found for platform ", NamedTextColor.RED)
                            .append(text(platform.name, NamedTextColor.AQUA))
                            .append(solidLine(prefix = "\n", suffix = ""))
                    )
                }

                audience.sendMessage(

                    text("\nFound download URL, starting installation from: ", NamedTextColor.GRAY)
                        .append(text(platform.name, NamedTextColor.AQUA))
                        .append(text("...", NamedTextColor.GRAY))

                )

                plugin.download(platforms.keys.first())

                audience.sendMessage(
                    text("\nSUCCESS:", NamedTextColor.GREEN)
                        .append(
                            text(" Downloaded plugin from ", NamedTextColor.GRAY)
                                .append(text(platform.name, NamedTextColor.AQUA))
                                .append(solidLine(prefix = "\n", suffix = ""))
                        )
                )
            } else {
                audience.sendMessage(
                    text("\nERROR:", NamedTextColor.RED)
                        .append(
                            text(" Multiple platforms found, click one to prompt install command", NamedTextColor.GRAY)
                                .decoration(TextDecoration.STRIKETHROUGH, false)
                                .appendNewline()
                        )
                )

                platforms.forEach { (platform, _) ->
                    audience.sendMessage(
                        text(" - ", NamedTextColor.GRAY)
                            .append(
                                text(platform.name, NamedTextColor.AQUA)
                            )
                            .hoverEvent(text("Click to install"))
                            .clickEvent(ClickEvent.suggestCommand("/pp install ${plugin.name} --platform ${platform.name}"))
                    )
                }

                audience.sendMessage(solidLine(prefix = "", suffix = ""))
            }

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
            var platformsSuffix = text(" (", NamedTextColor.DARK_GRAY)

            val platformSize = plugin.platforms.size

            plugin.platforms.keys.forEachIndexed { index, platform ->
                println("appending $platform")

                platformsSuffix = platformsSuffix.append(
                    text(
                        "${platform.name}${if (index < platformSize - 1) ", " else ""}",
                        NamedTextColor.DARK_GRAY
                    )
                        .hoverEvent(text("Click to install from ${platform.name}"))
                        .clickEvent(ClickEvent.suggestCommand("/pp install ${plugin.name} --platform ${platform.name}"))
                )
            }

            platformsSuffix = platformsSuffix.append(text(")", NamedTextColor.DARK_GRAY))

            audience.sendMessage(
                text(" - ", NamedTextColor.GRAY)
                    .append(
                        text(plugin.name, NamedTextColor.AQUA)
                            .hoverEvent(text("Click to install"))
                            .clickEvent(ClickEvent.suggestCommand("/pp install ${plugin.name}"))
                            .append(platformsSuffix)
                    )
            )
        }

        audience.sendMessage(solidLine(prefix = "", suffix = ""))
    }
}