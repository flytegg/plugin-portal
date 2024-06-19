package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.config.Config
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache.findFile
import gg.flyte.pluginportal.plugin.manager.MarketplacePluginCache
import gg.flyte.pluginportal.plugin.util.*
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.*

@Command("pp", "pluginportal", "ppm")
class DeleteSubCommand {

    @Subcommand("delete")
    @AutoComplete("@installedPluginSearch *")
    fun deleteCommand(
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
                "Multiple plugins found, click one to prompt delete command",
                plugins,
                "delete"
            )
        }
    }

    private fun handleSinglePlugin(audience: Audience, localPlugin: LocalPlugin) {
        val targetPlatform = localPlugin.platform
        val targetMessage = "${localPlugin.name} from $targetPlatform with ID ${localPlugin.id}"

        val file = localPlugin.findFile()
        if (file == null) {
            LocalPluginCache.deletePlugin(localPlugin)
            return audience.sendMessage(status(Status.FAILURE, "Plugin file not found"))
        }

        LocalPluginCache.deletePlugin(localPlugin)

        PortalLogger.log(audience, PortalLogger.Action.DELETE, targetMessage)

        audience.sendMessage(status(Status.SUCCESS, "Plugin deleted").boxed())
    }
}
