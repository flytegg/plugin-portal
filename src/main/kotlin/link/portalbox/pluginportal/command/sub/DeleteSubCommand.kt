package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.type.Message
import link.portalbox.pluginportal.util.delete
import link.portalbox.pluginportal.util.getMarketplaceCache
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

class DeleteSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size <= 1) {
            sender.sendMessage(Message.noPluginSpecified)
            return
        }

        if (!getMarketplaceCache().inverse().containsKey(args[1])) {
            sender.sendMessage(Message.pluginNotFound)
            return
        }

        val localPlugin = Data.installedPlugins.find { it.id == getMarketplaceCache().inverse()[args[1]] }
        if (localPlugin == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                MiniMessage.miniMessage().serialize(Message.pluginNotInstalled),
                Placeholder.component("%plugin%", Component.text(args[1]))))
            return
        }

        if (!delete(pluginPortal, localPlugin)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                MiniMessage.miniMessage().serialize(Message.pluginNotDeleted),
                Placeholder.component("%plugin%", Component.text(args[1]))))
            return
        }

        sender.sendMessage(MiniMessage.miniMessage().deserialize(
            MiniMessage.miniMessage().serialize(Message.pluginDeleted),
            Placeholder.component("%plugin%", Component.text(args[1]))))
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null
        return StringUtil.copyPartialMatches(
                args[1],
                Data.installedPlugins.map { getMarketplaceCache()[it.id] },
                mutableListOf()
        )
    }
}
