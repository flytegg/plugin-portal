package gg.flyte.pluginPortal.commands.server;

import com.github.ajalt.clikt.core.CliktCommand
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptConfirm
import gg.flyte.pluginPortal.commands.abstractClasses.ServerAPICommand
import gg.flyte.pluginPortal.type.server.ServerConfig
import gg.flyte.pluginPortal.type.server.ServerManager
import java.io.File

class DeleteServerCommand : ServerAPICommand(
    name = "delete",
    help = "Delete a server"
) {

    override fun finishCommand(server: ServerConfig) {
        val shouldContinue = KInquirer.promptConfirm(
            message = "Are you sure you want to delete ${server.name}?",
            false
        )

        if (shouldContinue) {
            File(ServerManager.getServerFolderDirectory(), server.name).apply {
                deleteRecursively()
                delete()
            }
        } else {
            echo("Server deletion has been cancelled.")
        }
    }
}