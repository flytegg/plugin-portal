package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.Config
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.type.language.Message.fillInVariables
import link.portalbox.pluginportal.util.*
import gg.flyte.pplib.type.MarketplacePlugin
import gg.flyte.pplib.util.getPluginFromId
import gg.flyte.pplib.util.getPluginFromName
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.net.URL

class UpdateAllSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {

        val needUpdating = mutableListOf<MarketplacePlugin>()
        for (plugin in Data.installedPlugins) {

            val marketplacePlugin = getPluginFromId(plugin.id) ?: continue
            if (marketplacePlugin.version != plugin.version) {
                needUpdating.add(marketplacePlugin)
            }
        }

        if (needUpdating.isEmpty()) {
            sender.sendMessage(Message.noPluginRequireAnUpdate)
            return
        }

        sender.sendMessage(Message.updatingPlugins)
        for (outdatedPlugin in needUpdating) {
            val localPlugin = Data.installedPlugins.find { it.id == outdatedPlugin.id } ?: continue

            val plugin: MarketplacePlugin = getPluginFromId(outdatedPlugin.id) ?: continue
            if (plugin.version == localPlugin.version) return

            if (!plugin.isValidDownload()) {
                sender.sendMessage(Message.downloadNotFound)
                return
            }

            if (!delete(pluginPortal, localPlugin)) {
                sender.sendMessage(Message.pluginNotUpdated.fillInVariables(arrayOf(plugin.name)))
                return
            }

            install(plugin, pluginPortal).run {
                sender.sendMessage(Message.pluginUpdated)
                sender.sendMessage(if (Config.startupOnInstall) Message.pluginAttemptedEnabling else Message.restartServerToEnablePlugin)
            }

        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String> {
        return mutableListOf()
    }
}