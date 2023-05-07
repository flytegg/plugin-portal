package link.portalbox.pluginportal.listener

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.type.Message
import link.portalbox.pplib.type.VersionType
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
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