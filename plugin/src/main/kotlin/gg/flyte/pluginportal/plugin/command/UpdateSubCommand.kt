package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.config.Config
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.manager.MarketplacePluginCache
import gg.flyte.pluginportal.plugin.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import revxrsal.commands.annotation.*

@Command("pp", "pluginportal", "ppm")
class UpdateSubCommand {

    @Subcommand("update")
    @AutoComplete("@installedPluginSearch *")
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

    private fun sendLocalPluginListMessage(audience: Audience, message: String, plugins: List<LocalPlugin>, command: String) {
        audience.sendMessage(startLine().appendSecondary(message).appendNewline())
        plugins.forEach { plugin ->
            val platformSuffix = textDark(" (${plugin.platform.name})")

            audience.sendMessage(
                textSecondary(" - ").appendPrimary(plugin.name)
                    .hoverEvent(text("Click to $command"))
                    .suggestCommand("/pp $command ${plugin.name} --platform ${plugin.platform.name}")
                    .append(platformSuffix)
            )
        }
        audience.sendMessage(endLine())
    }
}
