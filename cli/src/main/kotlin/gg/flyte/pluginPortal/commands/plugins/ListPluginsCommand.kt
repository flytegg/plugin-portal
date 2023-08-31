package gg.flyte.pluginPortal.commands.plugins

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.table.row
import com.github.ajalt.mordant.table.table
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerManager

class ListPluginsCommand : CliktCommand(
    name = "list",
    help = "List all plugins."
) {

    override fun run() {
        val activeServer = ServerManager.getActiveServer()

        if (activeServer == null) {
            echo("No active server found, use the command: ppcli server select")
            return
        }

        Config.terminal.println(table {
            header {
                row("Installed Plugins for server: ${activeServer.name}")
            }

            body {
                row {
                    StringBuilder().apply {
                        activeServer.installedPlugins.forEach { plugin ->
                            append(plugin.)
                            append(" ")
                            append(version)
                            append(" ")
                            append(it.platformType.name)
                            append("\n")
                        }
                    }
                }
            }

        })

        for (plugin in activeServer.installedPlugins) {
            echo(plugin)
        }


    }

}