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
        @Optional @Flag("id") idFlag: String? = null,
    ) {
        if (prefix == null && idFlag == null) {
            return sendFailureMessage(audience, "No plugin name or ID provided")
        }

        val plugins = LocalPluginCache
            .filter { plugin -> plugin.name.startsWith(prefix ?: "", ignoreCase = true) }
            .ifEmpty {
                return sendFailureMessage(audience, "No plugins found")
            }

        if (plugins.size == 1) {
            async {
                handleSinglePlugin(audience, plugins.first())
            }
        } else {
            sendLocalPluginListMessage(
                audience,
                "Multiple plugins found, click one to prompt update command",
                plugins,
                "update"
            )
        }
    }

    private fun handleSinglePlugin(audience: Audience, localPlugin: LocalPlugin) {
        // TODO: localPlugin.id is no longer the universal id, its the platform specific id

        val marketplacePlugin = MarketplacePluginCache.getFilteredPlugins(id = localPlugin.platformId)
            .firstOrNull() ?: return sendFailureMessage(audience, "Marketplace plugin not found")

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
