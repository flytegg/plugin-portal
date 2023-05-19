package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.Config
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.type.language.Message.fillInVariables
import link.portalbox.pluginportal.util.*
import gg.flyte.pplib.manager.MarketplacePluginManager
import gg.flyte.pplib.type.MarketplacePlugin
import gg.flyte.pplib.util.getPluginFromName
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import java.net.URL

class UpdateSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size >= 2) {
            val plugin = getPluginFromName(args[1])

            val localPlugin = Data.installedPlugins.find { it.id == plugin.id }
            if (localPlugin == null) {
                sender.sendMessage(Message.pluginNotFound)
                return
            }

            if (plugin.version == localPlugin.version) {
                sender.sendMessage(Message.pluginIsUpToDate.fillInVariables(arrayOf(plugin.name)))
                return
            }

            if (plugin.downloadURL.isEmpty() || plugin.downloadURL == "null") {
                sender.sendMessage(Message.downloadNotFound)
                return
            }


            if (!delete(pluginPortal, localPlugin)) {
                sender.sendMessage(Message.pluginNotUpdated.fillInVariables(arrayOf(plugin.name)))
                return
            }

            Bukkit.getScheduler().runTaskAsynchronously(pluginPortal, Runnable {
                install(plugin, URL(plugin.downloadURL))

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
                Data.installedPlugins.map { it.id },
                mutableListOf())
    }
}