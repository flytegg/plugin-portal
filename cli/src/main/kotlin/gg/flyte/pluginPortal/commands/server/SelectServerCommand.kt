package gg.flyte.pluginPortal.commands.server

import gg.flyte.pluginPortal.commands.abstractClasses.ServerAPICommand
import gg.flyte.pluginPortal.type.server.ServerConfig
import gg.flyte.pluginPortal.type.server.ServerManager

class SelectServerCommand : ServerAPICommand(
    name = "select",
    help = "Select a server"
) {

    override fun finishCommand(server: ServerConfig) {
        ServerManager.setActiveServer(server)
    }
}