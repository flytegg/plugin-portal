package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.util.calculateSHA1
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import java.io.File

@Command("pp", "pluginportal", "ppm")
class RecognizeSubCommand {

    @Subcommand("recognize")
    @AutoComplete("@pluginFileSearch *")
    fun recognizeCommand(audience: Audience, pluginFile: String) {
        val pluginFile = File("plugins", pluginFile)
        val sha1 = calculateSHA1(pluginFile)

        println(sha1)
    }

}