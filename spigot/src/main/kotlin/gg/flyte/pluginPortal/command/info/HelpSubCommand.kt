package gg.flyte.pluginPortal.command.info

import gg.flyte.pluginPortal.type.language.Message.toComponent
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Default
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.help.CommandHelp

@Command("pp", "pluginportal", "ppm", "pportal")
class HelpSubCommand {

    @Subcommand("help")
    fun onHelpCommand(sender: Audience, helpEntries: CommandHelp<String?>, @Default("1") page: Int) {
        for (entry in helpEntries.paginate(page, 7))
            sender.sendMessage(entry!!.toComponent())
    }

}