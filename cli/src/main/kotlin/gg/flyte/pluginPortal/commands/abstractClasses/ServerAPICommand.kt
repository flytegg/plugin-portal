package gg.flyte.pluginPortal.commands.abstractClasses

import com.github.ajalt.clikt.core.CliktCommand
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptList
import gg.flyte.pluginPortal.type.server.ServerManager
import gg.flyte.pluginPortal.type.server.ServerConfig
import gg.flyte.pluginPortal.util.promptBetterList
import java.io.File

abstract class ServerAPICommand(
    name: String,
    help: String
) : CliktCommand(
    name = name,
    help = help
) {
    override fun run() {

        if (ServerManager.getServerList().isEmpty()) {
            echo("No servers exist! Create one with /ppcli server create")
            return
        }

        val server = KInquirer.promptBetterList(
            "Select a server to manage:",
            ServerManager.getServerList()
        )

        val serverFile = File(ServerManager.getServerFolderDirectory(), server)
        if (!serverFile.exists()) {
            echo("Server does not exist!")
            return
        }

        finishCommand(ServerManager.getServerFromName(server))

    }

    abstract fun finishCommand(server: ServerConfig)
}