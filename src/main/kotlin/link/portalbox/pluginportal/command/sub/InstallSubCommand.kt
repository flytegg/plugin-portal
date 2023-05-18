package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.Config
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.type.language.Message.fillInVariables
import link.portalbox.pluginportal.util.*
import link.portalbox.pplib.util.getPluginFromName
import link.portalbox.pplib.util.requestPlugin
import link.portalbox.pplib.util.searchPlugins
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.net.URL

class InstallSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size <= 1) {
            sender.sendMessage(Message.noPluginSpecified)
            return
        }

        val plugin = getPluginFromName(args[1])
        if (plugin == null) {
            sender.sendMessage(Message.pluginNotFound)
            return
        }

        if (Data.installedPlugins.find { it.id == plugin.id } != null) {
            sender.sendMessage(Message.pluginAlreadyInstalled)
            return
        }

        if (plugin.isPremium) {
            sender.sendMessage(Message.pluginIsPremium)
            return
        }

        if (plugin.downloadURL.isEmpty() || plugin.downloadURL == "null") {
            sender.sendMessage(Message.downloadNotFound)
            requestPlugin(plugin.toRequestPlugin("External Download URL", sender.name))
            return
        }

        if (plugin.service != Config.marketplaceService) {
            sender.sendMessage(Message.serviceNotSupported.fillInVariables(arrayOf(Config.marketplaceService?.name ?: "UNKNOWN")))
            return
        }

        sender.sendMessage(Message.pluginIsBeingInstalled)

        Bukkit.getScheduler().runTaskAsynchronously(pluginPortal, Runnable {
            install(plugin, URL(plugin.downloadURL))
            sender.sendMessage(Message.pluginHasBeenInstalled.fillInVariables(arrayOf(plugin.name)))
            sender.sendMessage(if (Config.startupOnInstall) Message.pluginAttemptedEnabling else Message.restartServerToEnablePlugin)
        })
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
