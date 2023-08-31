package gg.flyte.pluginPortal.commands.plugins

import com.github.ajalt.mordant.table.table
import com.github.kinquirer.KInquirer
import gg.flyte.common.api.API
import gg.flyte.common.api.dataClasses.MarketplacePlugin
import gg.flyte.common.util.getVersionRange
import gg.flyte.common.util.installPlugin
import gg.flyte.common.util.isJARFileDownload
import gg.flyte.pluginPortal.commands.abstractClasses.PluginAPICommand
import gg.flyte.pluginPortal.type.server.ServerManager
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.util.promptBetterList

class SearchPluginsCommand : PluginAPICommand(
    name = "search",
    help = "Search for plugins on the marketplace."
) {
    override fun finishCommand(plugin: MarketplacePlugin) {
        val action = KInquirer.promptBetterList("What would you like to do?", listOf("Install", "Preview", "Exit"))

        when (action) {
            "Install" -> {
                val activeServer = ServerManager.getActiveServer()

                if (activeServer == null) {
                    echo("No active server found, use the command: ppcli server select")
                    return
                }

                val downloadUrl =
                    plugin.versions[activeServer.softwareType.primarySupportedPlatformType]?.get(plugin.versionData.latestVersion)?.downloadUrl

                if (downloadUrl.isNullOrEmpty()) {
                    echo("No download URL found for plugin: ${plugin.displayInfo.name} for platform: ${activeServer.softwareType.primarySupportedPlatformType}")
                    API.requestPluginById(plugin.id, activeServer.softwareType.primarySupportedPlatformType ?: return)
                    return
                }

                if (isJARFileDownload(downloadUrl)) {
                    println("Downloading plugin: ${plugin.displayInfo.name}")
                    installPlugin(
                        plugin,
                        downloadUrl,
                        activeServer.getPluginsFolder()
                    )

                    ServerManager.installPluginToServer(
                        plugin,
                        downloadUrl,
                        activeServer.getPluginsFolder(),
                        plugin.versionData.latestVersion,
                        activeServer.softwareType.primarySupportedPlatformType!!
                    )
                } else {
                    println("Invalid download URL: $downloadUrl")
                }
            }

            "Preview" -> {

                val platformVersions = StringBuilder().apply {
                    plugin.versions.forEach { (platform, versionInfo) ->
                        append("      -${platform.name}: ")
                        appendLine(versionInfo[plugin.versionData.latestVersion]?.supportedVersions?.toMutableList()
                            ?.let { getVersionRange(it) })
                    }
                }

                Config.terminal.println(table {
                    header { row(plugin.displayInfo.name) }
                    body {
                        row(
                            """   
    Current Service: ${plugin.primaryServiceType.name}                                                     
    Downloads: ${plugin.stats.downloads}
    Rating: ${plugin.stats.ratingAverage}/${plugin.stats.ratingCount}
    Price: ${if (plugin.stats.isPremium) "Premium: $${plugin.stats.price}" else "Free"}
    Latest Version (${plugin.versionData.latestVersion}):
$platformVersions

                            """
                        )
                    }
                })
            }

            "Exit" -> return
        }
    }
}