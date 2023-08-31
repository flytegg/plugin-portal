package gg.flyte.pluginPortal.commands.server.preset

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.table.table
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptConfirm
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerManager
import gg.flyte.pluginPortal.util.promptBetterList
import java.io.File

class DeletePresetCommand : CliktCommand(
    name = "delete",
    help = "Delete a preset"
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
                "Select a preset to delete:",
                presets.map { preset -> "${preset.name} | ${preset.version} | ${preset.softwareType.name}" },
            ).let { presetName ->
                val preset =
                    presets.find { preset -> "${preset.name} | ${preset.version} | ${preset.softwareType.name}" == presetName }!!

                KInquirer.promptConfirm(
                    message = "Are you sure you want to delete ${preset.name}?",
                    false
                ).let { shouldDelete ->
                    if (shouldDelete) {
                        File(ServerManager.getPresetsFolderDirectory(), "${preset.name}.json").delete()
                        Config.terminal.println(table {
                            body { row("${preset.name} has been deleted.") }
                        })
                    } else {
                        Config.terminal.println(table {
                            body { row("Preset deletion has been cancelled.") }
                        })
                    }
                }
            }
        }
    }
}