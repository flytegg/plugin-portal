package gg.flyte.pluginPortal.commands.plugins

import com.github.ajalt.clikt.core.CliktCommand
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

        for (plugin in activeServer.pluginInstallers) {
            echo(plugin)
        }



    }

}