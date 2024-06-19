package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.util.*
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand

@Command("pp", "pluginportal", "ppm")
class UpdateSubCommand {

    @Subcommand("update")
    @AutoComplete("@installedPluginSearch *")
    fun updateCommand(audience: Audience, name: String) {
        val plugins = LocalPluginCache.filter { it.name == name }

        if (plugins.isEmpty()) return sendFailureMessage(audience, "Plugin not found")
        if (plugins.size > 1) return sendFailureMessage(audience, "Multiple plugins found")

        val localPlugin = plugins.first()
        val marketplacePlugin = API.getPlugins(localPlugin.name).firstOrNull { it.id == localPlugin.id }
            ?: return sendFailureMessage(audience, "Marketplace plugin not found")

        audience.sendMessage(
            startLine().appendSecondary("Starting update of ").appendPrimary(localPlugin.name).appendSecondary("...")
        )

        val targetPlatform = localPlugin.platform
        val targetMessage = "${localPlugin.name} from $targetPlatform with ID ${localPlugin.id}"

        PortalLogger.log(audience, PortalLogger.Action.INITIATED_UPDATE, targetMessage)
        marketplacePlugin.download(targetPlatform, true)
        PortalLogger.log(audience, PortalLogger.Action.UPDATE, targetMessage)
    }
}
