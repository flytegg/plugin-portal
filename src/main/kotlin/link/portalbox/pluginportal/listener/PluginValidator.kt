package link.portalbox.pluginportal.listener

import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.type.Message
import link.portalbox.pluginportal.util.getMarketplaceCache
import link.portalbox.pluginportal.util.getSHA
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.io.File

class PluginValidator : Listener {
    private val removedPlugins = mutableListOf<String>()
    private var notified = false

    init {
        val installedShas = mutableListOf<String>()
        for (file in File("plugins").listFiles()!!) {
            if (!file.isDirectory) {
                installedShas.add(getSHA(file))
            }
        }

        val pluginsToDelete = mutableListOf<String>()
        for (plugin in Data.installedPlugins) {
            if (!installedShas.contains(plugin.fileSha)) {
                runCatching {
                    removedPlugins.add(getMarketplaceCache()[plugin.id]!!)
                    pluginsToDelete.add(plugin.id)
                }
            }
        }

        pluginsToDelete.forEach {
            Data.delete(it)
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (removedPlugins.isEmpty()) return
        if (notified) return
        if (!event.player.isOp) return

        val plugins = when (removedPlugins.size) {
            1 -> removedPlugins[0]
            2 -> "${removedPlugins[0]} and ${removedPlugins[1]}"
            else -> removedPlugins.dropLast(1).joinToString(", ") + " and " + removedPlugins.last()
        }

        event.player.sendMessage(MiniMessage.miniMessage().deserialize(
            MiniMessage.miniMessage().serialize(Message.playerManuallyRemovedPlugins),
            Placeholder.component("{plugins}", Component.text(plugins))))
        notified = true
    }
}