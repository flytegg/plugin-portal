package gg.flyte.pluginPortal.commands.plugins

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.table.table
import com.github.kinquirer.KInquirer
import gg.flyte.common.api.API
import gg.flyte.common.util.get256Hash
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerManager
import gg.flyte.pluginPortal.util.promptBetterList

class UpdatePluginCommand : CliktCommand(
    name = "update",
    help = "Update a plugin."
) {
    override fun run() {
        val activeServer = if (ServerManager.noServerFoundCheck()) return
        else ServerManager.getActiveServer()!!

        if (activeServer.installedPlugins.isEmpty()) {
            Config.terminal.println(table {
                header { row("No plugins found!") }
                body { row("Install one with /ppcli plugins") }
            })
            return
        }

        val localPlugin = KInquirer.promptBetterList(
            "Select a plugin to update: (${activeServer.installedPlugins.size})",
            activeServer.installedPlugins.map { "${it.name} (${it.id}) | ${it.primaryPlatformType} | ${it.version}" },
        ).let { pluginName ->
            activeServer.installedPlugins.find { it.name == pluginName.substringBefore(" (") }
        }

        val marketplacePlugin = API.getPluginById(localPlugin!!.id).body() ?: return

        if (localPlugin.version == marketplacePlugin.versionData.latestVersion ) {
            Config.terminal.println(table {
                body { row("Plugin is already up to date!") }
            })
            return
        } else {
            ServerManager.installPluginToServer(
                marketplacePlugin,
                marketplacePlugin.versions[localPlugin.primaryPlatformType]?.get(marketplacePlugin.versionData.latestVersion)!!.downloadUrl,
                activeServer.getPluginsFolder(),
                marketplacePlugin.versionData.latestVersion,
                localPlugin.primaryPlatformType
            )
        }
    }

}