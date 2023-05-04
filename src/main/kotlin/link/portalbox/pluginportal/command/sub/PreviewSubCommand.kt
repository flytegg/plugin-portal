package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.util.colorOutput
import link.portalbox.pluginportal.util.copyPartialMatchesWithService
import link.portalbox.pluginportal.util.getMarketplaceCache
import link.portalbox.pluginportal.util.sendPreview
import link.portalbox.pplib.manager.MarketplacePluginManager
import link.portalbox.pplib.type.MarketplaceService
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

class PreviewSubCommand : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size <= 1) {
            sender.sendMessage("&cPlease specify a plugin to preview!".colorOutput())
            return
        }

        var pluginName = ""
        if (args[1].contains(":")) {
            val split: List<String> = args[1].split(":")
            if (split.size == 2) {
                pluginName = "${split[0].uppercase()}:${split[1]}"
            }
        }

        if (!getMarketplaceCache().inverse().contains(pluginName)) {
            sender.sendMessage("&cPlugin does not exist.".colorOutput())
            return
        }

        sendPreview(sender, MarketplacePluginManager.getPlugin(getMarketplaceCache().inverse()[pluginName]!!))
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null
        return if (args[1].length <= 2) {
            mutableListOf("Keep Typing...")
        } else copyPartialMatchesWithService(args[1], getMarketplaceCache().values, mutableListOf()).toMutableList()
    }
}
