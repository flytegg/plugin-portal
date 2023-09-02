package gg.flyte.pluginPortal.command.info

import gg.flyte.pluginPortal.type.language.Message.toComponent
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Default
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.SecretCommand
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.help.CommandHelp

@Command("pp", "pluginportal", "ppm", "pportal")
class HelpSubCommand {

    @DefaultFor("pp help", "pp", "pluginportal", "ppm", "pportal")
    @Subcommand("help")
    fun onHelpCommand(sender: Audience, helpEntries: CommandHelp<String?>, @Optional page: Int?) {
        for (entry in helpEntries.paginate(page ?: 1, 7))
            sender.sendMessage(entry!!.toComponent())
    }

}