package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.command.SubCommandType
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.type.language.Message.fillInVariables
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender

class HelpSubCommand : SubCommand() {
    override fun execute(audience: Audience, commandSender: CommandSender, args: Array<out String>) {
        audience.sendMessage(Message.blankStrikeThroughWithWatermark)
        for (command in SubCommandType.values()) {
            audience.sendMessage(Message.helpCommandDisplay.fillInVariables(arrayOf(command.command, command.usage)))
        }

        audience.sendMessage(Message.blankStrikeThrough)
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        return null
    }
}