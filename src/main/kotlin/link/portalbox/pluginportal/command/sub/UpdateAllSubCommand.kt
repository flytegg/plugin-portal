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
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.net.URL

class UpdateAllSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {

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

        sender.sendMessage(Message.updatingPlugins)
        for (outdatedPlugin in needUpdating) {
            val id = getMarketplaceCache().inverse()[outdatedPlugin.name]
            val localPlugin = Data.installedPlugins.find { it.id == id } ?: return

            val plugin: MarketplacePlugin = MarketplacePluginManager.getPlugin(id!!)
            if (plugin.version == localPlugin.version) return

            if (plugin.downloadURL.isEmpty() || plugin.downloadURL == "null") {
                sender.sendMessage(Message.downloadNotFound)
                return
            }

            if (!delete(pluginPortal, localPlugin)) {
                sender.sendMessage(Message.pluginNotUpdated.fillInVariables(arrayOf(plugin.name)))
                return
            }

            Bukkit.getScheduler().runTaskAsynchronously(pluginPortal, Runnable {
                install(plugin, URL(plugin.downloadURL)!!)

                sender.sendMessage(Message.pluginUpdated)
                sender.sendMessage(if (Config.startupOnInstall) Message.pluginAttemptedEnabling else Message.restartServerToEnablePlugin)
            })
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String> {
        return mutableListOf()
    }
}