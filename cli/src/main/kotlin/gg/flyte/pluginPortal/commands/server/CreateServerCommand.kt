package gg.flyte.pluginPortal.commands.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.kinquirer.KInquirer
import gg.flyte.common.api.PaperMCAPI
import gg.flyte.common.type.service.ServerType
import gg.flyte.common.type.service.SoftwareType
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerConfig
import gg.flyte.pluginPortal.type.server.ServerManager
import gg.flyte.pluginPortal.type.server.ServerVersion
import gg.flyte.pluginPortal.util.promptBetterInput
import gg.flyte.pluginPortal.util.promptBetterList
import java.io.File

class CreateServerCommand : CliktCommand(
    name = "create",
    help = "Create a new server"
) {
    override fun run() {
        fun getServerName(): String {
            val serverName = KInquirer.promptBetterInput("Enter Server Name:")
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

        val softwareType = KInquirer.promptBetterList("Select Server Type:",
            ServerType.values()
                .filter { serverType -> serverType.containsSupportedSoftware() }
                .map { serverType -> serverType.getDisplayName() })
            .let { serverType ->
                KInquirer.promptBetterList(
                    "Select Server Software Type:",
                    SoftwareType.values()
                        .filter { it.softwareInterface != null }
                        .filter { it.serverType == ServerType.valueOf(serverType.substringBefore(" >")) }
                        .map { it.getDisplayName() }).let { SoftwareType.valueOf(it.substringBefore(" >")) }
            }

        val serverVersion = KInquirer.promptBetterList(
            "Server Version:",
            SoftwareType.valueOf(softwareType.name).softwareInterface!!.getVersions().reversed()
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
