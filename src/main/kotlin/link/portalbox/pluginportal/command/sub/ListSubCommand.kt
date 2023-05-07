package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.type.Message
import link.portalbox.pluginportal.type.Message.fillInVariables
import link.portalbox.pluginportal.util.getMarketplaceCache
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
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
            sender.sendMessage(Message.installedPlugin.fillInVariables(arrayOf(getMarketplaceCache()[plugin.id] ?: plugin.id)))
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        return null
    }
}