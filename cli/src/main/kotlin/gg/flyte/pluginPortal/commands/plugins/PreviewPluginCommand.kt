package gg.flyte.pluginPortal.commands.plugins

import gg.flyte.common.type.api.plugin.MarketplacePlugin
import gg.flyte.common.type.api.service.ServiceType
import gg.flyte.pluginPortal.commands.abstractClasses.PluginAPICommand
import java.awt.Desktop
import java.net.URI

class PreviewPluginCommand : PluginAPICommand(
    name = "preview",
    help = "Show plugin information for correct installation."
) {
    override fun finishCommand(plugin: MarketplacePlugin) {
        when (plugin.primaryServiceType) {
            ServiceType.SPIGOTMC -> {
                preview("https://www.spigotmc.org/resources/${plugin.id}")
            }

            else -> {
                echo("No preview available for this service type.")
            }
        }
    }

    private fun preview(url: String) {
        if (true) {
            println("Opening Plugin Page in browser...")
            Desktop.getDesktop().browse(URI("https://www.spigotmc.org/resources/9089"))
            println("To disable this feature, please visit the config.")
        } else {

        }
    }
}