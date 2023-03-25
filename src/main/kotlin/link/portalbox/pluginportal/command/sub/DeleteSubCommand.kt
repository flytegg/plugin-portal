package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.util.ChatColor.color
import link.portalbox.pluginportal.util.delete
import link.portalbox.pplib.manager.MarketplaceManager
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

class DeleteSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size <= 1) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &cPlease specify a plugin to delete!".color())
            return
        }

        if (!MarketplaceManager.marketplaceCache.inverse().containsKey(args[1])) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &cYou specified an invalid plugin.".color())
            return
        }

        val localPlugin = Data.installedPlugins.find { it.id == MarketplaceManager.getId(args[1]) }
        if (localPlugin == null) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &c${args[1]} &7is not installed.".color())
            return
        }

        if (!delete(pluginPortal, localPlugin)) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &c${args[1]} &7has not been deleted due to an error. If you just installed this plugin and didn't restart, that is probably the cause. Be sure to restart and run this command again.".color())
            return
        }

        sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &a${args[1]} &7has been deleted.".color())
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null
        return StringUtil.copyPartialMatches(
            args[1],
            Data.installedPlugins.map { MarketplaceManager.getName(it.id) },
            mutableListOf())
    }

}