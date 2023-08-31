package gg.flyte.pluginPortal.commands.server.preset

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.table.table
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerManager
import java.io.File

class ListPresetsCommand : CliktCommand(
    name = "list",
    help = "List all the server presets"
) {
    override fun run() {
        ServerManager.getPresetList().let { presets ->
            Config.terminal.println(table {
                header {
                    if (presets.isNotEmpty()) row("Name | Version | Software | Plugin Count")
                }

                body {
                    row(
                        StringBuilder().apply {
                            presets.forEach { preset ->
                                append(" - ${preset.name} | ${preset.version} | ${preset.softwareType.name} | ${preset.installedPlugins.size} \n")
                            }
                            if (presets.isEmpty()) append("No presets found! Create one with /ppcli server preset save")
                        }
                    )
                }
                footer { row("Total Preset Count: ${presets.size}") }
            })
        }
    }
}