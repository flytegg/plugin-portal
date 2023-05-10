package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.util.copyPartialMatchesWithService
import link.portalbox.pluginportal.util.getMarketplaceCache
import link.portalbox.pluginportal.util.sendPreview
import link.portalbox.pplib.manager.MarketplacePluginManager
import org.bukkit.command.CommandSender

class PreviewSubCommand : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size <= 1) {
            sender.sendMessage(Message.noPluginSpecified)
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
            sender.sendMessage(Message.pluginNotFound)
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
