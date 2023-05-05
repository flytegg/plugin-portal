package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.util.*
import link.portalbox.pplib.manager.MarketplacePluginManager
import link.portalbox.pplib.type.MarketplacePlugin
import link.portalbox.pplib.util.getPluginJSON
import link.portalbox.pplib.util.isDirectDownload
import link.portalbox.pplib.util.isJarFile
import link.portalbox.pplib.util.requestPlugin
import org.bukkit.command.CommandSender
import java.net.URL

class RequestSubCommand : SubCommand() {
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

        val plugin: MarketplacePlugin = MarketplacePluginManager.getPlugin(getMarketplaceCache().inverse()[pluginName]!!)

        var isJarFile = false
        runCatching {
            isJarFile = (isJarFile(URL(plugin.downloadURL)) || isJarFile(URL(getPluginJSON(plugin.id).get("alternateDownload").toString()?: "")))
        }

        if (isJarFile) {
            sender.sendMessage("&7This plugin is already supported.".colorOutput())
        } else {
            requestPlugin(plugin.toRequestPlugin("External Download, /pp request"))
            sender.sendMessage("&7Plugin has been requested.".colorOutput())
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null
        return if (args[1].length <= 2) {
            mutableListOf("Keep Typing...")
        } else copyPartialMatchesWithService(args[1], getMarketplaceCache().values, mutableListOf()).toMutableList()
    }
}