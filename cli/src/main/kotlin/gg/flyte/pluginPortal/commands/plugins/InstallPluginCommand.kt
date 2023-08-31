package gg.flyte.pluginPortal.commands.plugins

import com.github.kinquirer.KInquirer
import gg.flyte.common.api.dataClasses.MarketplacePlugin
import gg.flyte.common.type.service.PlatformType
import gg.flyte.common.type.service.findPlatformTypesFromGroup
import gg.flyte.common.util.installPlugin
import gg.flyte.pluginPortal.commands.abstractClasses.PluginAPICommand
import gg.flyte.pluginPortal.type.server.ServerManager
import gg.flyte.pluginPortal.util.promptBetterList

class InstallPluginCommand : PluginAPICommand(
    name = "install",
    help = "Install a Plugin."
) {
    override fun finishCommand(plugin: MarketplacePlugin) {
        val validDownloadPlatforms: HashSet<PlatformType> = hashSetOf()

        plugin.versions.keys.forEach { platformType ->
            platformType.platformGroup.forEach { platformGroup ->
                findPlatformTypesFromGroup(platformGroup)
                    .filter { plugin.versions.containsKey(it) }
                    .forEach { validDownloadPlatforms.add(it) }
            }
        }


        if (validDownloadPlatforms.isEmpty()) {
            println("No valid download platforms found for this plugin.")
            return
        }

        val activeServer = ServerManager.getActiveServer()

        if (activeServer == null) {
            println("No active server found, use the command: ppcli server select")
            return
        }

        if (validDownloadPlatforms.size == 1) {
            if (plugin.versions[validDownloadPlatforms.first()]?.isEmpty() == true) {
                println("No valid download platforms found for this plugin.")
                return
            }

            installPlugin(
                plugin,
                plugin.versions[validDownloadPlatforms.first()]?.values?.first()!!.downloadUrl,
                activeServer.getPluginsFolder()
            )

        }

        if (validDownloadPlatforms.size > 1) {
            val platformType = KInquirer.promptBetterList(
                "Select a platform to download the plugin for.",
                validDownloadPlatforms.map { it.name },
                pageSize = 7
            )

            if (platformType.isEmpty()) return

            if (plugin.versions[PlatformType.valueOf(platformType)]?.isEmpty() == true) {
                println("No valid download platforms found for this plugin.")
                return
            }

            ServerManager.installPluginToServer(
                plugin,
                plugin.versions[validDownloadPlatforms.first()]?.values?.first()!!.downloadUrl,
                activeServer.getPluginsFolder(),
                plugin.versionData.latestVersion,
                PlatformType.valueOf(platformType)
            )
        }


    }
}
