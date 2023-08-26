package gg.flyte.pluginPortal.commands.plugins

import gg.flyte.common.api.dataClasses.MarketplacePlugin
import gg.flyte.common.type.service.PlatformType
import gg.flyte.common.type.service.findPlatformTypesFromGroup
import gg.flyte.common.util.installPlugin
import gg.flyte.pluginPortal.commands.abstractClasses.PluginAPICommand

class InstallPluginCommand : PluginAPICommand(
    name = "install",
    help = "Install a Plugin."
) {
    override fun finishCommand(plugin: MarketplacePlugin) {
        val validDownloadPlatforms: ArrayList<PlatformType> = arrayListOf()

        findPlatformTypesFromGroup(plugin.serviceData.platformGroup)
            .filter { plugin.versions.containsKey(it) }
            .forEach { validDownloadPlatforms.add(it) }

        if (validDownloadPlatforms.isEmpty()) {
            println("No valid download platforms found for this plugin.")
            return
        }

        if (validDownloadPlatforms.size == 1) {
            installPlugin(plugin, validDownloadPlatforms[0])
            return
        }


    }
}
