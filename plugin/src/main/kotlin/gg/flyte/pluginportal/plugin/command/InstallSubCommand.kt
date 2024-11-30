package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.chat.*
import gg.flyte.pluginportal.plugin.command.lamp.MarketplacePluginSuggestionProvider
import gg.flyte.pluginportal.plugin.config.Config
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.manager.MarketplacePluginCache
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class InstallSubCommand {

    @Subcommand("install")
    @CommandPermission("pluginportal.manage.install")
    fun installCommand(
        audience: Audience,
        @Named("name") @SuggestWith(MarketplacePluginSuggestionProvider ::class) name: String,
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

        MarketplacePluginCache.installPlugin(
            audience,
            plugin,
            platformFlag ?: plugin.highestPriorityPlatform,
            Config.INSTALL_DIRECTORY
        )
    }
}
