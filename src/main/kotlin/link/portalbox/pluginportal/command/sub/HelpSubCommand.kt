package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.command.SubCommandType
import link.portalbox.pluginportal.type.Message
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender

class HelpSubCommand : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        sender.sendMessage(Message.blankStrikeThroughWithWatermark)
        for (command in SubCommandType.values()) {
            sender.sendMessage(
                MiniMessage.miniMessage().deserialize(
                    MiniMessage.miniMessage().serialize(Message.helpCommandDisplay)
                        .replace("%command%", command.command)
                        .replace("%usage%", command.usage)
                )
            )
        }
        sender.sendMessage(Message.blankStrikeThrough)
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        return null
    }
}