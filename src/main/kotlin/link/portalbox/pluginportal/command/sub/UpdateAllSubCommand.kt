package link.portalbox.pluginportal.command.sub

import gg.flyte.pplib.type.plugin.MarketplacePlugin
import gg.flyte.pplib.util.getPluginFromID
import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.Config
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.type.language.Message.fillInVariables
import link.portalbox.pluginportal.util.*
import net.kyori.adventure.audience.Audience
import org.bukkit.command.CommandSender

class UpdateAllSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
    override fun execute(audience: Audience, args: Array<out String>) {

        val needUpdating = mutableListOf<MarketplacePlugin>()
        for (plugin in Data.installedPlugins) {

            val marketplacePlugin = getPluginFromID(plugin.marketplacePlugin.id) ?: continue
            if (marketplacePlugin.version != plugin.marketplacePlugin.version) {
                needUpdating.add(marketplacePlugin)
            }
        }

        if (needUpdating.isEmpty()) {
            sender.sendMessage(Message.noPluginRequireAnUpdate)
            return
        }

        sender.sendMessage(Message.updatingPlugins)
        for (outdatedPlugin in needUpdating) {
            val localPlugin = Data.installedPlugins.find { it.marketplacePlugin.id == outdatedPlugin.id } ?: continue

            val plugin: MarketplacePlugin = getPluginFromID(outdatedPlugin.id) ?: continue
            if (plugin.version == localPlugin.marketplacePlugin.version) return

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