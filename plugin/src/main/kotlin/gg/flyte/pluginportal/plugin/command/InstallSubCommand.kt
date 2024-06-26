package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.chat.*
import gg.flyte.pluginportal.plugin.config.Config
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.manager.MarketplacePluginCache
import gg.flyte.pluginportal.plugin.util.async
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class InstallSubCommand {

    @Subcommand("install")
    @AutoComplete("@marketplacePluginSearch *")
    @CommandPermission("pluginportal.manage.install")
    fun installCommand(
        audience: Audience,
        @Optional prefix: String? = null,
        @Optional @Flag("platform") platformFlag: MarketplacePlatform? = null,
        @Optional @Flag("id") idFlag: String? = null,
    ) {
        if (prefix == null && idFlag == null) {
            return sendFailureMessage(audience, "No plugin name or ID provided")
        }

        async {
            val plugins = MarketplacePluginCache.getFilteredPlugins(
                prefix = prefix,
                platform = platformFlag,
                id = idFlag
            )

            if (plugins.isEmpty()) {
                sendFailureMessage(audience, "No plugins found")
            }

            if (plugins.size == 1) {
                handleSinglePlugin(audience, plugins.first(), platformFlag)
            } else {
                sendPluginListMessage(
                    audience,
                    "Multiple plugins found, click one to prompt install command",
                    plugins,
                    "install"
                )
            }
        }

    }

    private fun handleSinglePlugin(audience: Audience, plugin: Plugin, platformFlag: MarketplacePlatform?) {
        val platforms = plugin.platforms

        if (LocalPluginCache.hasPlugin(plugin)) {
            return sendFailureMessage(audience, "Plugin already installed, use the update command instead")
        }

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

        } else {
            audience.sendMessage(
                newline()
                    .append(
                        status(Status.FAILURE, "Multiple platforms found, click one to prompt install command")
                            .appendNewline()
                    )
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
