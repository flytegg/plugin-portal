package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import revxrsal.commands.annotation.*

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


        val plugins = API.getPlugins(prefix).ifEmpty {
            return audience.sendMessage(status(Status.FAILURE, "No plugins found").boxed())
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
                val entry = platforms.entries.first()

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

                val targetPlatform = platforms.keys.first()
                val targetMessage = "${plugin.name} from $targetPlatform with ID ${plugin.id}"


                PortalLogger.log(
                    audience,
                    PortalLogger.Action.INITIATED_INSTALL,
                    targetMessage
                ) // Put before download in-case of error
                plugin.download(targetPlatform)
                PortalLogger.log(
                    audience,
                    PortalLogger.Action.INSTALL,
                    targetMessage
                ) // Thoughts on this? Some cases may fail. Figured its best to show

                audience.sendMessage(
                    text("\nSUCCESS:", NamedTextColor.GREEN)
                        .append(
                            text(" Downloaded plugin from ", NamedTextColor.GRAY)
                                .appendPrimary(plugin.name)
                                .append(endLine())
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
                platformsSuffix =
                    platformsSuffix.appendDark("${platform.name}${if (index < platformSize - 1) ", " else ""}")
                        .hoverEvent(text("Click to install from ${platform.name}"))
                        .clickEvent(ClickEvent.suggestCommand("/pp install ${plugin.name} --platform ${platform.name}"))

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

        audience.sendMessage(endLine())
    }
}