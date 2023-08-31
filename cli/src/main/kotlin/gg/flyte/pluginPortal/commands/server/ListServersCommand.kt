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
            if (servers.isEmpty()) {
                echo("No servers found!")
                return
            }

            Config.terminal.println(table {
                header { row("Server Name") }
                body {
                    row(
                        StringBuilder().apply {
                            servers.forEach { server -> append(" - $server \n") }
                        }
                    )
                }
                footer { row("Total Server Count: ${servers.size}") }
            })
        }
    }
}