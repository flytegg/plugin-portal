package gg.flyte.pluginPortal.commands.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptInput
import com.github.kinquirer.components.promptList
import gg.flyte.common.api.PaperMCAPI
import gg.flyte.common.type.service.ServerType
import gg.flyte.common.type.service.SoftwareType
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerConfig
import gg.flyte.pluginPortal.type.server.ServerManager
import gg.flyte.pluginPortal.type.server.ServerVersion
import gg.flyte.pluginPortal.util.promptBetterList
import java.io.File

class CreateServerCommand : CliktCommand(
    name = "create",
    help = "Create a new server"
) {
    override fun run() {
        fun getServerName(): String {
            val serverName = KInquirer.promptInput("Server Name: ")
            if (serverName.isEmpty()) {
                echo("Server name cannot be empty!")
                return getServerName()
            }

            val serverFolder = File(ServerManager.getServerFolderDirectory(), serverName)
            if (serverFolder.exists()) {
                echo("Server already exists!")
                return getServerName()
            }

            return serverName
        }

        val serverName = getServerName()

        val softwareType = KInquirer.promptList("Select Server Type:",
            ServerType.entries.map { it.getDisplayName() }).let { serverType ->
            KInquirer.promptList(
                "Select Server Software Type:",
                SoftwareType.entries
                    .filter { it.serverType == ServerType.valueOf(serverType.substringBefore(" >")) }
                    .map { it.getDisplayName() }).let { SoftwareType.valueOf(it.substringBefore(" >")) }
        }

        if (listOf(
                SoftwareType.PAPER,
                SoftwareType.TRAVERTINE,
                SoftwareType.WATERFALL,
                SoftwareType.VELOCITY,
                SoftwareType.FOLIA
            ).contains(softwareType)
        ) {
            val serverVersion = KInquirer.promptBetterList(
                "Server Version:",
                PaperMCAPI.getProject(softwareType.name.lowercase()).body()?.versions!!.map { it!! }.reversed()
            )
        }


        val serverVersion = KInquirer.promptList(
            "Server Version:",
            ServerVersion.entries.map { it.name.replace("V", "").replace("_", ".") }.reversed()
        )

        ServerConfig(
            serverName,
            softwareType,
            serverVersion,
            Config.userConfig.autoUpdatePlugins,
        ).apply {
            ServerManager.createServer(this)
            getPluginsFolder()

            if (Config.userConfig.selectServerUponCreation) ServerManager.setActiveServer(this)
        }
    }
}
