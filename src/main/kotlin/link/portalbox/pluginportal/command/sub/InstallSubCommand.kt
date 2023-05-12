package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.Config
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.type.language.Message.fillInVariables
import link.portalbox.pluginportal.util.*
import link.portalbox.pplib.manager.MarketplacePluginManager
import link.portalbox.pplib.type.MarketplacePlugin
import link.portalbox.pplib.type.PostError
import link.portalbox.pplib.util.getURL
import link.portalbox.pplib.util.requestPlugin
import link.portalbox.pplib.util.sendError
import link.portalbox.pplib.util.startErrorCatcher
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class InstallSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
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

        if (!getMarketplaceCache().inverse().containsKey(pluginName)) {
            sender.sendMessage(Message.pluginNotFound)
            return
        }

        val id: String = getMarketplaceCache().inverse()[pluginName]?: return
        if (Data.installedPlugins.find { it.id.equals(id) } != null) {
            sender.sendMessage(Message.pluginAlreadyInstalled)
            return
        }

        val plugin: MarketplacePlugin = MarketplacePluginManager.getPlugin(id)
        if (plugin.isPremium) {
            sender.sendMessage(Message.pluginIsPremium)
            return
        }

        if (plugin.downloadURL.isEmpty()) {
            sender.sendMessage(Message.downloadNotFound)
            addValueToPieChart(Chart.MOST_INVALID_DOWNLOADS, plugin.id)
            requestPlugin(plugin.toRequestPlugin("External Download URL", sender.name))
            return
        }

        if (plugin.service != Config.marketplaceService) {
            sender.sendMessage(Message.serviceNotSupported.fillInVariables(arrayOf(Config.marketplaceService?.name ?: "UNKNOWN")))
            return
        }

        sender.sendMessage(Message.pluginIsBeingInstalled)

        Bukkit.getScheduler().runTaskAsynchronously(pluginPortal, Runnable {
            install(plugin, getURL(plugin.downloadURL)!!)
            sender.sendMessage(Message.pluginHasBeenInstalled.fillInVariables(arrayOf(plugin.name)))
            sender.sendMessage(if (Config.startupOnInstall) Message.pluginAttemptedEnabling else Message.restartServerToEnablePlugin)
        })
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null
        return if (args[1].length <= 2) {
            mutableListOf(Message.keepTyping)
        } else {
            val completion = copyPartialMatchesWithService(args[1], getMarketplaceCache().values, mutableListOf()).toMutableList()
            if (completion.size == 0) { return mutableListOf(Message.loadingCache) }

            return completion
        }

    }
}
