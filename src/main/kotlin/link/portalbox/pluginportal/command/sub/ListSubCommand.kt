package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.util.ChatColor.color
import link.portalbox.pluginportal.util.ChatColor.colorOutput
import link.portalbox.pplib.manager.MarketplaceManager
import link.portalbox.pplib.manager.MarketplacePluginManager
import org.bukkit.command.CommandSender

class ListSubCommand : SubCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        val installedPlugins = Data.installedPlugins
        if (installedPlugins.isEmpty()) {
            sender.sendMessage("&7You have no plugins installed or recognised by Plugin Portal.".colorOutput())
            return
        }

        sender.sendMessage("&7Listing all installed plugins...".colorOutput())
        for (plugin in installedPlugins) {
            sender.sendMessage("&a+ &b${MarketplacePluginManager.marketplaceCache[plugin.id]}".color())
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? { return null }

}