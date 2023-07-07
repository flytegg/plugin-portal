package link.portalbox.pluginportal.listener

import gg.flyte.pplib.type.version.VersionType
import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.type.language.Message
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class UpdateListener(private val pluginPortal: PluginPortal) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (!event.player.isOp) return;
        if (pluginPortal.versionType == VersionType.LATEST || pluginPortal.versionType == VersionType.PATCH) return;

        Message.audiences.player(event.player).sendMessage(Message.playerOutdatedPluginPortal)
    }

}