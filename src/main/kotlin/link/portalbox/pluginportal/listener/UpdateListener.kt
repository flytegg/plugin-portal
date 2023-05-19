package link.portalbox.pluginportal.listener

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.type.language.Message
import gg.flyte.pplib.type.VersionType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class UpdateListener(private val pluginPortal: PluginPortal) : Listener {

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        if (!e.player.isOp) return;
        if (pluginPortal.versionType == VersionType.LATEST || pluginPortal.versionType == VersionType.PATCH) return;

        e.player.sendMessage(Message.playerOutdatedPluginPortal)
    }

}