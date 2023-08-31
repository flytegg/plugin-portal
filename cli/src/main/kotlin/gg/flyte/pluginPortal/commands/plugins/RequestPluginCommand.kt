package gg.flyte.pluginPortal.commands.plugins

import com.github.ajalt.mordant.table.table
import gg.flyte.common.api.API
import gg.flyte.common.api.dataClasses.MarketplacePlugin
import gg.flyte.pluginPortal.commands.abstractClasses.PluginAPICommand
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerManager

class RequestPluginCommand : PluginAPICommand(
    name = "request",
    help = "Request a Plugin to be supported."
) {
    override fun finishCommand(plugin: MarketplacePlugin) {

        Config.terminal.println(table {
            body { row("${plugin.displayInfo.name} has been requested to be updated.") }
        })

        API.requestPluginById(plugin.id, ServerManager.getActiveServer()?.softwareType?.primarySupportedPlatformType ?: return)
    }
}