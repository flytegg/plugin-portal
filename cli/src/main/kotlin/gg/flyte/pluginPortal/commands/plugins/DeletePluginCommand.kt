package gg.flyte.pluginPortal.commands.plugins

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.table.table
import com.github.kinquirer.KInquirer
import gg.flyte.common.util.get256Hash
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerManager
import gg.flyte.pluginPortal.util.promptBetterList

class DeletePluginCommand: CliktCommand(
    name = "delete",
    help = "Remove a plugin from the server."
) {
    override fun run() {
        val activeServer = if (ServerManager.noServerFoundCheck()) return
        else ServerManager.getActiveServer()!!

        if (activeServer.installedPlugins.isEmpty()) {
            Config.terminal.println(table {
                header { row("No plugins found!") }
                body { row("Install one with /ppcli plugins") }
            })
            return
        }

        val plugin = KInquirer.promptBetterList(
            "Select a plugin to delete.",
            activeServer.installedPlugins.map { "${it.name} (${it.id}) | ${it.primaryPlatformType} | ${it.version}" },
        ).let { pluginName ->
            activeServer.installedPlugins.find { it.name == pluginName.substringBefore(" (") }
        }

        for (file in activeServer.getPluginsFolder().listFiles()!!.filter { it.isFile }) {
            if (file.get256Hash() == plugin!!.sha256Hash) {
                file.delete()

                Config.terminal.println(table {
                    body { row("Deleted plugin: ${plugin.name}") }
                })

                activeServer.installedPlugins.removeIf { it.name == plugin.name }
                activeServer.save()
            }
        }

    }
}