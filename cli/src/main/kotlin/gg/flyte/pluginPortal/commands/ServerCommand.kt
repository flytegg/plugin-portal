package gg.flyte.pluginPortal.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptInput
import com.github.kinquirer.components.promptList
import gg.flyte.common.type.service.ServerType
import gg.flyte.common.type.service.SoftwareType
import gg.flyte.pluginPortal.type.server.ServerManager.createServer
import gg.flyte.pluginPortal.type.server.ServerManager.getServerFolderDirectory
import gg.flyte.pluginPortal.type.server.ServerManager.setActiveServer
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerConfig
import gg.flyte.pluginPortal.type.server.*
import gg.flyte.pluginPortal.type.server.ServerManager.startServer
import java.io.File

class ServerCommand : CliktCommand(
    name = "server",
    help = "Manage the Server"
) {
    override fun run() = Unit
}



class ServerSettings : CliktCommand(
    name = "settings",
    help = "Modify the server settings"
) {
    override fun run() {
        echo("starting server")
    }
}






class PresetCommand : CliktCommand(
    name = "preset",
    help = "Manage the server presets"
) {
    override fun run() = Unit
}

class ListPreset : CliktCommand(
    name = "list",
    help = "List all the server presets"
) {
    override fun run() {
        File("presets").walk().forEach {
            echo(it.name.removeSuffix(".json"))
        }
    }
}



class LoadPreset : CliktCommand(
    name = "load",
    help = "Load a preset"
) {
    override fun run() {
        echo("loading preset")
    }
}

class DeletePreset : CliktCommand(
    name = "delete",
    help = "Delete a preset"
) {
    override fun run() {
        echo("deleting preset")
    }
}
