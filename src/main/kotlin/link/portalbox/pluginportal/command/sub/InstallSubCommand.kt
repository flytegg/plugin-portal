package link.portalbox.pluginportal.command.sub

import gg.flyte.pplib.util.getPluginFromName
import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.Config
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.type.language.Message.fillInVariables
import link.portalbox.pluginportal.util.*
import gg.flyte.pplib.util.searchPlugins
import net.kyori.adventure.audience.Audience
import org.bukkit.command.CommandSender

class InstallSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
    override fun execute(audience: Audience, args: Array<out String>) {
        if (args.size <= 1) {
            sender.sendMessage(Message.noPluginSpecified)
            return
        }

        val plugin = getPluginFromName(args[1]) ?: run {
            sender.sendMessage(Message.pluginNotFound)
            return
        }

        if (Data.installedPlugins.find { it.marketplacePlugin.id == plugin.id } != null) {
            sender.sendMessage(Message.pluginAlreadyInstalled)
            return
        }

        if (plugin.isPremium) {
            sender.sendMessage(Message.pluginIsPremium)
            return
        }

        if (!plugin.isValidDownload()) {
            sender.sendMessage(Message.downloadNotFound)
            return
        }

        if (plugin.service != Config.marketplaceService) {
            sender.sendMessage(
                Message.serviceNotSupported.fillInVariables(
                    arrayOf(
                        Config.marketplaceService?.name ?: "UNKNOWN"
                    )
                )
            )
            // return
        }

        sender.sendMessage(Message.pluginIsBeingInstalled)

        install(plugin, pluginPortal).run {
            sender.sendMessage(Message.pluginHasBeenInstalled.fillInVariables(arrayOf(plugin.name)))
            sender.sendMessage(if (Config.startupOnInstall) Message.pluginAttemptedEnabling else Message.restartServerToEnablePlugin)
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null

        return if (args[1].length <= 2) {
            mutableListOf(Message.keepTyping)
        } else {
            val completion = searchPlugins(args[1])
            if (completion.isEmpty()) {
                mutableListOf(Message.noPluginsFound)
            } else {
                completion.toMutableList()
            }
        }
    }
}
