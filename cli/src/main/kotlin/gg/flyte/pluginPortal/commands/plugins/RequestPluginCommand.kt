package gg.flyte.pluginPortal.commands.plugins

import gg.flyte.common.api.dataClasses.MarketplacePlugin
import gg.flyte.pluginPortal.commands.abstractClasses.PluginAPICommand

class RequestPluginCommand : PluginAPICommand(
    name = "request",
    help = "Request a Plugin to be supported."
) {
    override fun finishCommand(plugin: MarketplacePlugin) {
        println(plugin.displayInfo.name + " - has been chosen")
    }


}