package gg.flyte.pluginportal.plugin.command

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand

@Command("pp", "pluginportal", "ppm")
class PluginPortalCommand {

    @Subcommand("view")
    fun viewSubCommand(sender: Audience) {
        sender.sendMessage(text("Viewed plugin"))
    }

}