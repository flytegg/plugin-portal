package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.chat.*
import gg.flyte.pluginportal.plugin.config.Config
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.manager.MarketplacePluginCache
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
        name: String,
        @Optional @Named("platform") platform: MarketplacePlatform? = null,
        @Switch("byId") byId: Boolean = false,
    ) {
        MarketplacePluginCache.handlePluginSearchFeedback(
            audience,
            name,
            platform,
            byId,
            ifSingle = { handleSinglePlugin(audience, it, platform) }, // Can slightly optimise by adding quick isInstalled check first
            ifMore = {
                sendPluginListMessage(
                    audience,
                    "Multiple plugins found, click one to prompt install command",
                    it,
                    "install"
                )
            }
        )
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
                        .suggestCommand("/pp install ${plugin.name} ${platform.name}")
                )
            }
            audience.sendMessage(solidLine(prefix = "", suffix = ""))
        }
    }
}
