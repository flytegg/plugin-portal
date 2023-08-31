package gg.flyte.pluginPortal.commands.plugins

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.table.row
import com.github.ajalt.mordant.table.table
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerManager
import jdk.internal.org.jline.utils.AttributedStringBuilder
import jdk.internal.org.jline.utils.AttributedStringBuilder.append

class ListPluginsCommand : CliktCommand(
    name = "list",
    help = "List all plugins."
) {

    override fun run() {
        if (ServerManager.noServerFoundCheck()) return
        val activeServer = ServerManager.getActiveServer()!!

        Config.terminal.println(table {
            header { row("Name (ID) | Platform | Version") }
            body {
                row(
                    StringBuilder().apply {
                        activeServer.installedPlugins
                            .map { plugin -> " - ${plugin.name} (${plugin.id}) | ${plugin.primaryPlatformType.name} | ${plugin.version} \n" }
                            .forEach { append(it) }

                        if (activeServer.installedPlugins.isEmpty()) AttributedStringBuilder.append("No plugins found! Install one with /ppcli plugins")
                    }
                )
            }

            footer { row("Total Plugins: ${activeServer.installedPlugins.size} | Server Name: ${activeServer.name}") }
        })

    }

}
