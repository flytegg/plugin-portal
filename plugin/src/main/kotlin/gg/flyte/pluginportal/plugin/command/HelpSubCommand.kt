package gg.flyte.pluginportal.plugin.command

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Subcommand

@Command("pp", "pluginportal", "ppm")
class HelpSubCommand {

    @Subcommand("help")
    fun helpCommand(audience: Audience) {
        audience.sendMessage(text("Helping..."))
    }

    @DefaultFor("~")
    fun help(audience: Audience) = helpCommand(audience)
}