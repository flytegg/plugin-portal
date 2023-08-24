package gg.flyte.pluginPortal.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptInput
import com.github.kinquirer.components.promptList
import gg.flyte.common.type.service.PlatformType
import gg.flyte.common.type.service.ServerType
import gg.flyte.common.type.service.SoftwareType
import gg.flyte.pluginPortal.manager.ServerManager
import gg.flyte.pluginPortal.manager.ServerManager.createServer
import gg.flyte.pluginPortal.manager.ServerManager.getActiveServer
import gg.flyte.pluginPortal.manager.ServerManager.getServerFolderDirectory
import gg.flyte.pluginPortal.`object`.Config
import gg.flyte.pluginPortal.`object`.serializer.SerializedConfig
import gg.flyte.pluginPortal.`object`.serializer.SerializedServer
import gg.flyte.pluginPortal.`object`.server.*
import java.io.File
import java.net.http.HttpClient

class ServerCommand : CliktCommand(
    name = "server",
    help = "Manage the Server"
) {
    override fun run() = Unit
}

class StartServer : CliktCommand(
    name = "start",
    help = "Start the Server"
) {
    override fun run() {
        echo("starting server")
    }
}

class ServerSettings : CliktCommand(
    name = "settings",
    help = "Modify the server settings"
) {
    override fun run() {
        echo("starting server")
    }
}

class CreateServer : CliktCommand(
    name = "create",
    help = "Create a new server"
) {
    override fun run() {
        var server = getActiveServer()

        if (server != null) {
            echo("A server already exists!")
            return
        }

        val serverName = KInquirer.promptInput("Server Name: ")

        val serverVersion = KInquirer.promptList(
            "Server Version:",
            ServerVersion.entries.map { it.name.replace("V", "").replace("_", ".") }.reversed()
        ).let { ServerVersion.valueOf("V$it".replace(".", "_")) }

        val softwareType = KInquirer.promptList("Select Server Type:",
            ServerType.entries.map { it.getDisplayName() }).let { serverType ->
                KInquirer.promptList(
                    "Select Server Software Type:",
                    SoftwareType.entries
                        .filter { it.serverType == ServerType.valueOf(serverType.substringBefore(" >")) }
                        .map { it.getDisplayName() }).let { SoftwareType.valueOf(it.substringBefore(" >")) }
            }




        server = SerializedServer(
            serverName,
            softwareType,
            serverVersion,
            Config.serializedConfig.autoUpdatePlugins,
        )

        createServer(server)
    }
}

class SelectServer : CliktCommand(
    name = "select",
    help = "Select a server"
) {
    override fun run() {
        val server = KInquirer.promptList(
            "Select a server to manage:",
            getServerFolderDirectory().listFiles()?.map { it.name } ?: listOf("Exit")
        )

        println(getServerFolderDirectory().listFiles())
        println(getServerFolderDirectory().absolutePath)

        val serverFile = File(getServerFolderDirectory(), server)
        if (!serverFile.exists()) {
            echo("Server does not exist!")
            return
        }

        ServerManager.activeServerFile = serverFile
    }
}

class DeleteServer : CliktCommand(
    name = "delete",
    help = "Delete a server"
) {
    override fun run() {
        echo("deleting server")
    }
}

class ListServers : CliktCommand(
    name = "list",
    help = "List all the servers"
) {
    override fun run() {
        File("servers").walk().forEach {
            echo(it.name.removeSuffix(".json"))
        }
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

class SavePreset : CliktCommand(
    name = "save",
    help = "Save the current server settings as a preset"
) {
    override fun run() {

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
