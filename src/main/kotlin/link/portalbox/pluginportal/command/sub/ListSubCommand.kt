package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.util.ChatColor.color
import org.bukkit.command.CommandSender

class ListSubCommand : SubCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>) {

        val installedPlugins = Data.installedPlugins
        if (installedPlugins.isEmpty()) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &7You have no plugins installed via Plugin Portal.".color())
            return
        }

        sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &7Listing all installed plugins...".color())
        for (plugin in installedPlugins) {
            // GET NAME FROM ID IN API
            sender.sendMessage("&a+ &7namehere")
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>) {
        TODO("Not yet implemented")
    }

}