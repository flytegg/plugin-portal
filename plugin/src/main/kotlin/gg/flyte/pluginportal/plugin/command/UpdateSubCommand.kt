package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.plugin.chat.*
import gg.flyte.pluginportal.plugin.command.lamp.InstalledPluginSuggestionProvider
import gg.flyte.pluginportal.plugin.config.Config
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.manager.MarketplacePluginCache
import gg.flyte.pluginportal.plugin.util.async
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class UpdateSubCommand {

    @Subcommand("update")
    @CommandPermission("pluginportal.maintain.update")
    fun updateCommand(
        audience: Audience,
        @Named("name") @SuggestWith(InstalledPluginSuggestionProvider::class) name: String,
        @Switch("byId") byId: Boolean = false, // @Suggest("--byId", "--ignoreOutdated", "-b", "-i", "-bi")
        @Switch("ignoreOutdated") @Suggest() ignoreOutdated: Boolean = false,
    ) {
        LocalPluginCache.searchPluginsWithFeedback(
            audience,
            name,
            byId,
            ifSingle = { plugin: LocalPlugin -> handleSinglePlugin(audience, plugin, ignoreOutdated) }.async(),
            ifMore = {
                sendLocalPluginListMessage(audience, "Multiple plugins found, click one to prompt update command", it,"update")
            },

        )
    }

    private fun handleSinglePlugin(audience: Audience, localPlugin: LocalPlugin, ignoreOutdated: Boolean) {
        val marketplacePlugin = MarketplacePluginCache.getPluginById(localPlugin.platform, localPlugin.platformId)
            ?: return run { // They possibly manually changed ID or our database down?
                audience.sendFailure("Update Failed: Plugin not found in marketplace")

                val target = "${localPlugin.name} with ID ${localPlugin.platformId} on ${localPlugin.platform}"
                PortalLogger.log(audience, PortalLogger.Action.FAILED_UPDATE, target)
                IllegalArgumentException("Failed Attempting to update $target").printStackTrace()
            }

        if (localPlugin.version == marketplacePlugin.platforms[localPlugin.platform]?.download?.version && !ignoreOutdated) {
            audience.sendFailure("Plugin is already up to date")
            return
        }

        audience.sendMessage(
            startLine()
                .appendSecondary("Starting update of ")
                .appendPrimary(localPlugin.name)
                .appendSecondary("...")
                .appendNewline()
        )

        val targetPlatform = localPlugin.platform
        val targetMessage = "${localPlugin.name} from $targetPlatform with ID ${localPlugin.platformId}"

        PortalLogger.log(audience, PortalLogger.Action.INITIATED_UPDATE, targetMessage)

        MarketplacePluginCache.installPlugin(
            audience,
            marketplacePlugin,
            targetPlatform,
            Config.UPDATE_DIRECTORY
        )

        // Upon updating, old and new plugin would show up in /pp update. This removes the old one
        LocalPluginCache.filter { it.platformId == localPlugin.platformId }.let { plugins ->
            if (plugins.size == 2) {
                val updatedPlugin = plugins.maxBy { plugin -> plugin.installedAt }

                plugins.forEach { plugin ->
                    if (plugin != updatedPlugin) {
                        LocalPluginCache.addToUpdatedPluginMap(updatedPlugin, plugin)
                        LocalPluginCache.remove(plugin)
                    }
                }
            }
        }

        LocalPluginCache.save()

        PortalLogger.log(audience, PortalLogger.Action.UPDATE, targetMessage)
    }
}
