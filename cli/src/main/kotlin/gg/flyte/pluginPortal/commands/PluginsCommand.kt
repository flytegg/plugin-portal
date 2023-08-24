package gg.flyte.pluginPortal.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptList
import gg.flyte.common.api.API
import gg.flyte.common.type.service.PlatformType
import gg.flyte.common.util.downloadFileAsync
import gg.flyte.common.util.isDirectDownload
import gg.flyte.pluginPortal.manager.ServerManager.getActiveServer
import java.awt.Desktop
import java.net.URI

class Plugins : CliktCommand(
    name = "plugins",
    help = "Manage Server's Plugins"
) {
    override fun run() = Unit
}

class PreviewPlugin : CliktCommand(
    name = "preview",
    help = "Show plugin information for correct installation."
) {
    override fun run() {

        if (true) {
            echo("Opening Plugin Page in browser...")
            Desktop.getDesktop().browse(URI("https://www.spigotmc.org/resources/9089"))
            echo("To disable this feature, please visit the config.")
        } else {
            echo("Plugin info: ")
        }

    }
}

class InstallPlugin : CliktCommand(
    name = "install",
    help = "Install latest plugin version from marketplace."
) {
    override fun run() = echo("managing install plugins")
}

class UpdatePlugin : CliktCommand(
    name = "update",
    help = "Update an installed plugin to the latest version."
) {
    override fun run() = echo("managing update plugins")
}

class ListPlugins : CliktCommand(
    name = "list",
    help = "List locally installed plugins."
) {
    override fun run() = echo("managing list plugins")
}

class DeletePlugin : CliktCommand(
    name = "delete",
    help = "Remove a plugin from the server."
) {
    override fun run() = echo("managing delete plugins")
}

class RequestPlugins : CliktCommand(
    name = "request",
    help = "Request a Plugin to be supported."
) {
    override fun run() = echo("managing request plugins")
}

class SearchPlugins : CliktCommand(
    name = "search",
    help = "Search for plugins on the marketplace."
) {
    val name: String by option().prompt("Plugin Name").help("The name of the plugin to search for.")

    override fun run() {
        val plugins = API.searchForPluginsByName(
            name,
            "PAPER",
            25,
            0
        )

        val pluginName = KInquirer.promptList(
            "Found ${plugins.body()?.pagination?.total} plugins. Select a plugin to find out more, more specific search terms may be needed to find a plugin.",
            plugins.body()?.result?.map { "${it.displayInfo.name} - ${it.displayInfo.description}" }?.apply {
                toMutableList().add("")
            } ?: listOf("Exit"),
            pageSize = 7,
        )

        if (pluginName.isEmpty() || pluginName == "Exit") return

        val plugin = plugins.body()?.result?.find { "${it.displayInfo.name} - ${it.displayInfo.description}" == pluginName }
            ?: return

        val action = KInquirer.promptList("What would you like to do?", listOf("Install", "Preview", "Exit"))

        when (action) {
            "install" -> {
                val activeServer = getActiveServer()
                if (activeServer == null) {
                    echo("No active server found, use the command: pp server select")
                    return
                }

                val downloadUrl = plugin.versions[activeServer.softwareType.primarySupportedPlatformType]?.get(plugin.versionData.latestVersion)?.downloadUrl

                if (downloadUrl.isNullOrEmpty()) {
                    echo("No download URL found for plugin: ${plugin.displayInfo.name}")
                    API.requestPluginById(plugin.id, activeServer.softwareType.primarySupportedPlatformType ?: return)
                    return
                }

                if (isDirectDownload(downloadUrl)) {
                    downloadFileAsync(downloadUrl, activeServer.getPluginsFolder()) {
                        if (it) {
                            echo("Successfully downloaded plugin: ${plugin.displayInfo.name}")
                        } else {
                            echo("Failed to download plugin: ${plugin.displayInfo.name}")
                        }
                    }
                }
            }

            "preview" -> {

            }

            "exit" -> return
        }
    }
}