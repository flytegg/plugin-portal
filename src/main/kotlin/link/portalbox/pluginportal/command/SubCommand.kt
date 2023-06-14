package link.portalbox.pluginportal.command

import net.kyori.adventure.audience.Audience
import org.bukkit.command.CommandSender

abstract class SubCommand {
    abstract fun execute(audience: Audience, commandSender: CommandSender, args: Array<out String>)
    abstract fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>?
}