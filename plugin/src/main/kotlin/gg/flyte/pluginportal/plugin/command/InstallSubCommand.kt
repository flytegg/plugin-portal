package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.config.Config
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.manager.MarketplacePluginCache
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

        if (prefix == null && idFlag == null) {
            return sendFailureMessage(audience, "No plugin name or ID provided")
        }

        val plugins = MarketplacePluginCache.getFilteredPlugins(
            prefix = prefix,
            platform = platformFlag,
            id = idFlag
        )

        if (plugins.isEmpty()) {
            return sendFailureMessage(audience, "No plugins found")
        }

        if (plugins.size == 1) {
            handleSinglePlugin(audience, plugins.first(), platformFlag)
        } else {
            sendPluginListMessage(audience, "Multiple plugins found, click one to prompt install command", plugins, "install")
        }
    }

    private fun handleSinglePlugin(audience: Audience, plugin: Plugin, platformFlag: MarketplacePlatform?) {
        val platforms = plugin.platforms
        audience.sendMessage(
            startLine()
                .appendSecondary("Starting installation of ")
                .appendPrimary(plugin.name)
                .appendSecondary("...")
                .appendNewline()
        )

        if (platforms.size == 1 || platformFlag != null) {
            val platform = platformFlag ?: platforms.keys.first()

            MarketplacePluginCache.installPlugin(
                audience,
                plugin,
                platform,
                Config.INSTALL_DIRECTORY
            )



//            audience.sendMessage(
//                textSecondary("Found download URL, starting installation from: ")
//                    .appendPrimary(platform.name).appendSecondary("...")
//                    .appendNewline()
//            )

//            val targetMessage = "${plugin.name} from ${platform.name} with ID ${plugin.id}"
//            PortalLogger.log(audience, PortalLogger.Action.INITIATED_INSTALL, targetMessage)
//            plugin.download(platform)
//            PortalLogger.log(audience, PortalLogger.Action.INSTALL, targetMessage)

//            audience.sendMessage(
//                text("\nSUCCESS:", NamedTextColor.GREEN)
//                    .append(text(" Downloaded plugin from ", NamedTextColor.GRAY))
//                    .appendPrimary(plugin.name).append(endLine())
//            )
        } else {
            audience.sendMessage(
                text("\n")
                    .append(status(Status.FAILURE, "Multiple platforms found, click one to prompt install command")
                        .appendNewline())
            )
            platforms.forEach { (platform, _) ->
                audience.sendMessage(
                    textSecondary(" - ").appendPrimary(platform.name)
                        .hoverEvent(text("Click to install"))
                        .suggestCommand("/pp install ${plugin.name} --platform ${platform.name}")
                )
            }
            audience.sendMessage(solidLine(prefix = "", suffix = ""))
        }
    }
}
