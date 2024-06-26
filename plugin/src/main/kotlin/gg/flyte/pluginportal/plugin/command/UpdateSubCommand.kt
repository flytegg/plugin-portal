package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.plugin.chat.*
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
    @AutoComplete("@installedPluginSearch *")
    @CommandPermission("pluginportal.maintain.update")
    fun updateCommand(
        audience: Audience,
        @Optional prefix: String? = null,
        @Optional @Flag("id") platformId: String? = null,
    ) {
        LocalPluginCache.searchPluginsWithFeedback(
            audience,
            prefix,
            platformId,
            ifSingle = { plugin: LocalPlugin -> handleSinglePlugin(audience, plugin) }.async(),
            ifMore = {
                sendLocalPluginListMessage(audience, "Multiple plugins found, click one to prompt update command", it,"update")
            }
        )
    }

    private fun handleSinglePlugin(audience: Audience, localPlugin: LocalPlugin) {
        val marketplacePlugin = MarketplacePluginCache.getPluginById(localPlugin.platform, localPlugin.platformId)
            ?: return run { // They possibly manually changed ID or our database down?
                audience.sendFailure("Update Failed: Plugin not found in marketplace")

                val target = "${localPlugin.name} with ID ${localPlugin.platformId} on ${localPlugin.platform}"
                PortalLogger.log(audience, PortalLogger.Action.FAILED_UPDATE, target)
                IllegalArgumentException("Failed Attempting to update $target").printStackTrace()
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

        PortalLogger.log(audience, PortalLogger.Action.UPDATE, targetMessage)
    }
}
