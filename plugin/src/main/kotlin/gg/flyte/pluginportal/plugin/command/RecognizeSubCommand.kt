package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.chat.Status
import gg.flyte.pluginportal.plugin.chat.boxed
import gg.flyte.pluginportal.plugin.chat.status
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class RecognizeSubCommand {

    @Subcommand("recognize")
    @AutoComplete("@pluginFileSearch *")
    @CommandPermission("pluginportal.manage.recognize")
    fun recognizeCommand(audience: Audience, @Optional pluginFileName: String? = null) {
        audience.sendMessage(status(Status.FAILURE, "This command is only available for Plugin Portal Premium.").boxed())
    }
}