package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
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
                    textSecondary("\nFound download URL, starting installation from: ")
                        .appendPrimary(platform.name)
                        .appendSecondary("...")
                )

                val targetPlatform = platforms.keys.first()
                val targetMessage = "${plugin.name} from $targetPlatform with ID ${plugin.id}"

                PortalLogger.log(
                    audience,
                    PortalLogger.Action.INITIATED_INSTALL,
                    targetMessage
                )
                plugin.download(targetPlatform)
                PortalLogger.log(
                    audience,
                    PortalLogger.Action.INSTALL,
                    targetMessage
                )

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
                    status(Status.FAILURE, "Multiple platforms found, click one to prompt install command")
                        .appendNewline()
                )

                platforms.forEach { (platform, _) ->
                    audience.sendMessage(
                        textSecondary(" - ")
                            .appendPrimary(plugin.name)
                            .hoverEvent(text("Click to install"))
                            .suggestCommand("/pp install ${plugin.name} --platform ${platform.name}")
                    )
                }

                audience.sendMessage(solidLine(prefix = "", suffix = ""))
            }

            return
        }

        audience.sendMessage(
            startLine()
                .appendSecondary("Multiple plugins found, click one to prompt install command")
                .appendNewline()
        )

        plugins.forEach { plugin ->
            var platformsSuffix = textDark(" (")
            val platformSize = plugin.platforms.size

            plugin.platforms.keys.forEachIndexed { index, platform ->
                platformsSuffix =
                    platformsSuffix.appendDark("${platform.name}${if (index < platformSize - 1) ", " else ""}")
                        .hoverEvent(text("Click to install from ${platform.name}"))
                        .suggestCommand("/pp install ${plugin.name} --platform ${platform.name}")

            }

            platformsSuffix = platformsSuffix.appendDark(")")

            audience.sendMessage(
                textSecondary(" - ")
                    .appendPrimary(plugin.name)
                    .hoverEvent(text("Click to install"))
                    .suggestCommand("/pp install ${plugin.name}")
                    .append(platformsSuffix)
            )
        }

        audience.sendMessage(endLine())
    }
}