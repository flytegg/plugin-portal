package link.portalbox.pluginportal.command

import org.bukkit.command.CommandSender

abstract class SubCommand {

    abstract fun execute(sender: CommandSender, args: Array<out String>)

    abstract fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>?

}