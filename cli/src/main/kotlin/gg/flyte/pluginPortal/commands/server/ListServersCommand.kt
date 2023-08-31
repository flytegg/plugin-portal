package gg.flyte.pluginPortal.commands.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.table.table
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerManager

class ListServersCommand : CliktCommand(
    name = "list",
    help = "List all the servers"
) {
    override fun run() {

        ServerManager.getServerList().let { servers ->
            Config.terminal.println(table {
                header {
                    if (servers.isNotEmpty()) row("Name | Version | Software | Plugin Count")
                }
                body {
                    row(
                        StringBuilder().apply {
                            servers.forEach { server ->
                                append(" - ${server.name} | ${server.version} | ${server.softwareType.name} | ${server.installedPlugins.size} \n")
                            }
                            if (servers.isEmpty()) append("No servers found!")
                        }
                    )
                }
                footer {
                    if (servers.isNotEmpty()) row("Total Server Count: ${servers.size}")
                    else row("Create one with /ppcli server create")
                }
            })
        }
    }
}