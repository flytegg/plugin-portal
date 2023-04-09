package link.portalbox.pluginportal.listener

import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.util.color
import link.portalbox.pluginportal.util.getSHA
import link.portalbox.pplib.manager.MarketplacePluginManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.io.File

class PluginValidator : Listener {
    private val removedPlugins = mutableListOf<String>()
    private var notified = false

    init {
        val installedShas = mutableListOf<String>()
        for (file in File("plugins").listFiles()) {
            if (!file.isDirectory) {
                installedShas.add(getSHA(file))
            }
        }

        val pluginsToDelete = mutableListOf<Int>()
        for (plugin in Data.installedPlugins) {
            if (!installedShas.contains(plugin.fileSha)) {
                removedPlugins.add(MarketplacePluginManager.marketplaceCache[plugin.id]!!)
                pluginsToDelete.add(plugin.id)
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
            1 -> "${removedPlugins[0]}"
            2 -> "${removedPlugins[0]} and ${removedPlugins[1]}"
            else -> removedPlugins.dropLast(1).joinToString(", ") + " and " + removedPlugins.last()
        }

        event.player.sendMessage("&7&l[&b&lPP&7&l] &8&l> &7We noticed you manually removed &c$plugins&7. To prevent issues, we have removed ${if (removedPlugins.size > 1) "them" else "it"} from our data store too.".color())
        notified = true
    }
}