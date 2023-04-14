package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.util.colorOutput
import link.portalbox.pluginportal.util.delete
import link.portalbox.pplib.manager.MarketplacePluginManager
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

class DeleteSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size <= 1) {
            sender.sendMessage("&cPlease specify a plugin to delete!".colorOutput())
            return
        }

        if (!MarketplacePluginManager.marketplaceCache.inverse().containsKey(args[1])) {
            sender.sendMessage("&cYou specified an invalid plugin.".colorOutput())
            return
        }

        val localPlugin = Data.installedPlugins.find { it.id == MarketplacePluginManager.marketplaceCache.inverse()[args[1]] }
        if (localPlugin == null) {
            sender.sendMessage("&c${args[1]} &7is not installed.".colorOutput())
            return
        }

        if (!delete(pluginPortal, localPlugin)) {
            sender.sendMessage("&c${args[1]} &7has not been deleted due to an error. If you just installed this plugin and didn't restart, that is probably the cause. Be sure to restart and run this command again.".colorOutput())
            return
        }

        sender.sendMessage("&a${args[1]} &7has been deleted.".colorOutput())
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null
        return StringUtil.copyPartialMatches(
                args[1],
                Data.installedPlugins.map { MarketplacePluginManager.marketplaceCache[it.id] },
                mutableListOf()
        )
    }
}
