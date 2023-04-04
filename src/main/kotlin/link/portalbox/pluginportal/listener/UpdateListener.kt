package link.portalbox.pluginportal.listener

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.util.ChatColor.color
import link.portalbox.pluginportal.util.install
import link.portalbox.pluginportal.util.setupMetrics
import link.portalbox.pplib.type.MarketplacePlugin
import link.portalbox.pplib.type.SpigetPlugin
import link.portalbox.pplib.util.PPApiUtil
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bstats.bukkit.Metrics
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.regex.Matcher
import java.util.regex.Pattern

class UpdateListener(private val pluginPortal: PluginPortal) : Listener {

    init {
        val matcher: Matcher = Pattern.compile("\"version\":\"([\\d\\.]+)\"").matcher(PPApiUtil.getPPVersion())
        val versionNumber: String? = if (matcher.find()) matcher.group(1) else null
        pluginPortal.LATEST_VERSION = versionNumber == pluginPortal.description.version
        if (!pluginPortal.LATEST_VERSION) {
            pluginPortal.logger.severe("You are running an outdated version of Plugin Portal! We will attempt to update for you.")
            pluginPortal.logger.severe("Download Link: https://www.spigotmc.org/resources/plugin-portal.108700")
            pluginPortal.logger.severe("Current Version: ${pluginPortal.description.version}")
            pluginPortal.logger.severe("Latest Version: $versionNumber")
            val plugin: MarketplacePlugin = SpigetPlugin(108700).marketplacePlugin
            install(plugin, plugin.downloadURL)
            pluginPortal.pluginLoader.disablePlugin(pluginPortal)
        } else {
            pluginPortal.logger.fine("Having problems? Join our support Discord @ discord.gg/portalbox.")
            setupMetrics(Metrics(pluginPortal, 18005))
        }
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        if (!e.player.isOp) return;
        if (pluginPortal.LATEST_VERSION) return;

        val component = TextComponent("&7&l[&b&lPP&7&l] &8&l> &7Plugin Portal needs to be updated. Please download the latest version from: &b&l[CLICK HERE]".color())
        component.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/plugin-portal.108700/")
        e.player.spigot().sendMessage(component)
    }

}