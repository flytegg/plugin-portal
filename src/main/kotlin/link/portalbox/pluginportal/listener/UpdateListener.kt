package link.portalbox.pluginportal.listener

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.util.color
import link.portalbox.pluginportal.util.install
import link.portalbox.pluginportal.util.isLatestVersion
import link.portalbox.pplib.manager.MarketplacePluginManager
import link.portalbox.pplib.type.MarketplaceService
import link.portalbox.pplib.util.getLatestPPVersion
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern

class UpdateListener(private val pluginPortal: PluginPortal) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (!event.player.isOp) return
        if (!isLatestVersion(pluginPortal)) return

        val component = TextComponent("&7&l[&b&lPP&7&l] &8&l> &7Plugin Portal needs to be updated. Please download the latest version from: &b&l[CLICK HERE]".color())
        component.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/plugin-portal.108700/")
        event.player.spigot().sendMessage(component)
    }
}