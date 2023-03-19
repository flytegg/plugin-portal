package link.portalbox.pluginportal.listener

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.util.ChatColor.color
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ConnectionListener(val pluginPortal: PluginPortal) : Listener {

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {

        if (!e.player.isOp) return;
        if (pluginPortal.LATEST_VERSION) return;

        val component = TextComponent("&7&l[&b&lPP&7&l] &8&l> &7Plugin Portal needs to be updated. Please download the latest version from: &b&l[CLICK HERE]".color())
        component.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, "https://portalbox.link")
        e.player.spigot().sendMessage(component)

    }

}