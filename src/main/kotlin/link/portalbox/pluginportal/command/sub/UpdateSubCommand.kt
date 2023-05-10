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
import link.portalbox.pplib.util.getURL
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

class UpdateSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size >= 2) {
            val id = getMarketplaceCache().inverse()[args[1]]
            val localPlugin = Data.installedPlugins.find { it.id == id }
            if (localPlugin == null) {
                sender.sendMessage(Message.pluginNotFound)
                return
            }

            val plugin: MarketplacePlugin = MarketplacePluginManager.getPlugin(id!!)
            if (plugin.version == localPlugin.version) {
                sender.sendMessage(Message.pluginIsUpToDate.fillInVariables(arrayOf(plugin.name)))
                return
            }

            if (plugin.downloadURL.isEmpty()) {
                sender.sendMessage(Message.downloadNotFound)
                return
            }


            if (!delete(pluginPortal, localPlugin)) {
                sender.sendMessage(Message.pluginNotUpdated.fillInVariables(arrayOf(plugin.name)))
                return
            }

            Bukkit.getScheduler().runTaskAsynchronously(pluginPortal, Runnable {
                install(plugin, getURL(plugin.downloadURL)!!)

                sender.sendMessage(Message.pluginUpdated)
                sender.sendMessage(if (Config.startupOnInstall) Message.pluginAttemptedEnabling else Message.restartServerToEnablePlugin)
            })
            return
        }

        val needUpdating = mutableListOf<MarketplacePlugin>()
        for (plugin in Data.installedPlugins) {
            val spigetPlugin = MarketplacePluginManager.getPlugin(plugin.id)
            if (spigetPlugin.version != plugin.version) {
                needUpdating.add(spigetPlugin)
            }
        }

        if (needUpdating.isEmpty()) {
            sender.sendMessage(Message.noPluginRequireAnUpdate)
            return
        }

        sender.sendMessage(Message.listingAllOutdatedPlugins)
        for (spigetPlugin in needUpdating) {
            sender.sendMessage(Message.installedPlugin.fillInVariables(arrayOf(spigetPlugin.name)))
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null
        return StringUtil.copyPartialMatches(
                args[1],
                Data.installedPlugins.map { getMarketplaceCache()[it.id] },
                mutableListOf())
    }
}