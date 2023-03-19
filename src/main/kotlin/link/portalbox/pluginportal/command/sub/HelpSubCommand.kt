package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.command.SubCommandType
import link.portalbox.pluginportal.util.ChatColor.color
import org.bukkit.command.CommandSender

class HelpSubCommand : SubCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        sender.sendMessage("&8<&m                      &7 [&b&lPP&7] &m                      &8>".color())
        for (command in SubCommandType.values()) {
            sender.sendMessage("&8- &b&l${command.command}&8: &7${command.usage}".color())
        }
        sender.sendMessage("\n&8&m                                                      ".color())
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>) { }

}