package gg.flyte.pluginPortal.commands.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.table.table
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerManager

class InfoServerCommand: CliktCommand(
    name = "info",
    help = "Get info about the active server."
) {
    override fun run() {
        val activeServer = if (ServerManager.noServerFoundCheck()) return
        else ServerManager.getActiveServer()!!

        val displayPlugins = StringBuilder().apply {
            activeServer.installedPlugins
                .map { plugin -> "      - ${plugin.name} (${plugin.id}) | ${plugin.primaryPlatformType.name} | ${plugin.version} \n" }
                .forEach { append(it) }

            if (activeServer.installedPlugins.isEmpty()) append("No plugins found! Install one with /ppcli plugins")
        }

        Config.terminal.println(table {
            header { row("Server Name: ${activeServer.name}") }
            body { row(
                """
    Server Version: ${activeServer.version}
    Server Type: ${activeServer.softwareType.name}
    Server Path: ${activeServer.getDirectory().absolutePath}
  
    Server Plugins (${activeServer.installedPlugins.size}):
$displayPlugins

                """
            )}
            footer { row("Total Server Count: ${ServerManager.getServerList().size}") }
        })
    }
}