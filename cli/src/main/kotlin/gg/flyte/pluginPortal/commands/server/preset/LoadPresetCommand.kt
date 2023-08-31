package gg.flyte.pluginPortal.commands.server.preset

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.table.table
import com.github.kinquirer.KInquirer
import gg.flyte.pluginPortal.commands.abstractClasses.ServerAPICommand
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerConfig
import gg.flyte.pluginPortal.type.server.ServerManager
import gg.flyte.pluginPortal.util.promptBetterInput
import gg.flyte.pluginPortal.util.promptBetterList

class LoadPresetCommand : CliktCommand(
    name = "load",
    help = "Load a preset"
) {

    override fun run() {
        ServerManager.getPresetList().let { presets ->
            if (presets.isEmpty()) {
                Config.terminal.println(table {
                    header { row("No presets found!") }
                    body { row("Create one with /ppcli server preset save") }
                })

                return
            }

            KInquirer.promptBetterList(
                "Select a preset to load:",
                presets.map { preset -> "${preset.name} | ${preset.version} | ${preset.softwareType.name}" },
            ).let { presetName ->
                val preset =
                    presets.find { preset -> "${preset.name} | ${preset.version} | ${preset.softwareType.name}" == presetName }!!
                ServerManager.loadPreset(preset)
            }
        }
    }
}