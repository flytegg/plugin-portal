package gg.flyte.pluginPortal.commands.plugins

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.table.table
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerManager

class ListPluginsCommand : CliktCommand(
    name = "list",
    help = "List all plugins."
) {

    override fun run() {
        val activeServer = if (ServerManager.noServerFoundCheck()) return
        else ServerManager.getActiveServer()!!

        Config.terminal.println(table {
            header {
                if (activeServer.installedPlugins.isNotEmpty()) row("Name (ID) | Platform | Version")
            }
            body {
                row(
                    StringBuilder().apply {
                        activeServer.installedPlugins
                            .map { plugin -> " - ${plugin.name} (${plugin.id}) | ${plugin.primaryPlatformType.name} | ${plugin.version} \n" }
                            .forEach { append(it) }

                        if (activeServer.installedPlugins.isEmpty()) append("No plugins found! Install one with /ppcli plugins")
                    }
                )
            }

            footer { row("Total Plugins: ${activeServer.installedPlugins.size} | Server Name: ${activeServer.name}") }
        })

    }

}
