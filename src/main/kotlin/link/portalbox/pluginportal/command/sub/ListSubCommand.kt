package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.type.language.Message.fillInVariables
import gg.flyte.pplib.util.getPluginIdFromName
import org.bukkit.command.CommandSender

class ListSubCommand : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        val installedPlugins = Data.installedPlugins
        if (installedPlugins.isEmpty()) {
            sender.sendMessage(Message.noPluginsInstalled)
            return
        }

        sender.sendMessage(Message.listingAllPlugins)
        for (plugin in installedPlugins) {
            sender.sendMessage(Message.installedPlugin.fillInVariables(arrayOf(getPluginIdFromName(plugin.id))))
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        return null
    }
}