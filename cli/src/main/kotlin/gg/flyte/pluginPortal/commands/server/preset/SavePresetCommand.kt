package gg.flyte.pluginPortal.commands.server.preset

import com.github.ajalt.clikt.core.CliktCommand
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptConfirm
import gg.flyte.common.util.GSON
import gg.flyte.pluginPortal.type.server.ServerManager
import java.io.File

class SavePresetCommand : CliktCommand(
    name = "save",
    help = "Save the current server settings as a preset"
) {
    override fun run() {
        File(
            ServerManager.getPresetsFolderDirectory(),
            "${ServerManager.getActiveServer()?.name}.json"
        ).let { file ->
            if (file.exists()) {
                KInquirer.promptConfirm(
                    message = "A preset with this name already exists. Do you want to overwrite it?"
                ).let { overwrite ->
                    if (overwrite) {
                        file.writeText(GSON.toJson(ServerManager.getActiveServer()))
                    }
                }
            } else {
                file.createNewFile()
                file.writeText(GSON.toJson(ServerManager.getActiveServer()))
            }

            echo("Preset with name ${ServerManager.getActiveServer()?.name} has been saved!")
        }
    }
}