package gg.flyte.pluginPortal.commands.abstractClasses

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.table.table
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptList
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerManager
import gg.flyte.pluginPortal.type.server.ServerConfig
import gg.flyte.pluginPortal.util.promptBetterList
import java.io.File

abstract class ServerAPICommand(
    name: String,
    help: String,
    private val checkForServer: Boolean = true
) : CliktCommand(
    name = name,
    help = help
) {
    override fun run() {

        if (ServerManager.getServerList().isEmpty()) {
            Config.terminal.println(table {
                header { row("No Servers found") }
                body { row("Create one with /ppcli server create") }
            })
            return
        }

        if (checkForServer && ServerManager.noServerFoundCheck()) return

        val server = KInquirer.promptBetterList(
            "Select a server to manage:",
            ServerManager.getServerNameList()
        )

        val serverFile = File(ServerManager.getServerFolderDirectory(), server)
        if (!serverFile.exists()) {
            echo("Server does not exist!") // This should never happen
            return
        }

        finishCommand(ServerManager.getServerFromName(server))

    }

    abstract fun finishCommand(server: ServerConfig)
}