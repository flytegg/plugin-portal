package gg.flyte.pluginPortal.commands.server

import com.github.ajalt.clikt.core.CliktCommand
import gg.flyte.pluginPortal.type.server.ServerManager

class StartServerCommand : CliktCommand(
    name = "start",
    help = "Start the Server"
) {
    override fun run() {
        ServerManager.getActiveServer().let { server ->
            if (server == null) {
                echo("No active server found!")
                return
            }
            ServerManager.startServer(server)
        }
    }
}