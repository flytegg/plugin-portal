package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.util.ChatColor.color
import link.portalbox.pplib.manager.MarketplaceManager
import org.bukkit.command.CommandSender

class ListSubCommand : SubCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        val installedPlugins = Data.installedPlugins
        if (installedPlugins.isEmpty()) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &7You have no plugins installed or recognised by Plugin Portal.".color())
            return
        }

        sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &7Listing all installed plugins...".color())
        for (plugin in installedPlugins) {
            sender.sendMessage("&a+ &b${MarketplaceManager.getName(plugin.id)}".color())
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? { return null }

}