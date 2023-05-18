package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.util.*
import link.portalbox.pplib.manager.MarketplacePluginManager
import link.portalbox.pplib.type.MarketplacePlugin
import link.portalbox.pplib.util.*
import org.bukkit.command.CommandSender
import java.net.URL

class RequestSubCommand : SubCommand() {
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

        val plugin = getPluginFromName(args[1])
        if (plugin == null) {
            sender.sendMessage(Message.pluginNotFound)
            return
        }

        var isJarFile = false
        runCatching {
            isJarFile = (isJarFile(URL(plugin.downloadURL)) || isJarFile(URL(getAPIPlugin(plugin.id).alternateDownload)))
        }

        if (isJarFile) {
            sender.sendMessage(Message.pluginIsSupported)
        } else {
            requestPlugin(plugin.toRequestPlugin("External Download, /pp request", sender.name))
            sender.sendMessage(Message.pluginRequested)
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