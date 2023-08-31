package gg.flyte.pluginPortal.commands.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.table.table
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerManager
import jdk.internal.org.jline.utils.AttributedStringBuilder
import jdk.internal.org.jline.utils.AttributedStringBuilder.append

class ListServersCommand : CliktCommand(
    name = "list",
    help = "List all the servers"
) {
    override fun run() {

        ServerManager.getServerList().let { servers ->
            Config.terminal.println(table {
                header { row("Server Name") }
                body {
                    row(
                        StringBuilder().apply {
                            servers.forEach { server -> append(" - $server \n") }

                            if (servers.isEmpty()) AttributedStringBuilder.append("No servers found! Create one with /ppcli server create")
                        }
                    )
                }
                footer { row("Total Server Count: ${servers.size}") }
            })
        }
    }
}