package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.plugin.chat.*
import gg.flyte.pluginportal.plugin.config.Config
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.manager.MarketplacePluginCache
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
            handleSinglePlugin(audience, plugins.first())
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

        val marketplacePlugin = MarketplacePluginCache.getFilteredPlugins(id = localPlugin.id)
            .firstOrNull() ?: return sendFailureMessage(audience, "Marketplace plugin not found")

        audience.sendMessage(
            startLine()
                .appendSecondary("Starting update of ")
                .appendPrimary(localPlugin.name)
                .appendSecondary("...")
                .appendNewline()
        )

        val targetPlatform = localPlugin.platform
        val targetMessage = "${localPlugin.name} from $targetPlatform with ID ${localPlugin.id}"

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
