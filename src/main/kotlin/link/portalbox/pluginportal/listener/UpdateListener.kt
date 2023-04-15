package link.portalbox.pluginportal.listener

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.util.colorOutput
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
        if (pluginPortal.versionType != VersionType.LATEST) return;

        runCatching {
            val component = TextComponent("&7Plugin Portal needs to be updated. Please download the latest version from: &b&l[CLICK HERE]".colorOutput())
            component.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/plugin-portal.108700/")
            e.player.spigot().sendMessage(component)
        }
    }

}