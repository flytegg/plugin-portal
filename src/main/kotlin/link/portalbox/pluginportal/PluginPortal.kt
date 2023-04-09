package link.portalbox.pluginportal

import link.portalbox.pluginportal.command.PPCommand
import link.portalbox.pluginportal.file.Config
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.listener.PluginValidator
import link.portalbox.pluginportal.listener.UpdateListener
import link.portalbox.pluginportal.util.install
import link.portalbox.pluginportal.util.isLatestVersion
import link.portalbox.pluginportal.util.setupMetrics
import link.portalbox.pplib.manager.MarketplacePluginManager
import link.portalbox.pplib.manager.MarketplacePluginManager.loadIndex
import link.portalbox.pplib.service.SpigotMCService
import link.portalbox.pplib.type.MarketplaceService
import link.portalbox.pplib.util.getLatestPPVersion
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin
import java.net.URL

class PluginPortal : JavaPlugin() {
    var LATEST_VERSION = true

    override fun onEnable() {
        MarketplacePluginManager.registerService(MarketplaceService.SPIGOTMC, SpigotMCService())
        loadIndex()

        if (!isLatestVersion(this)) {
            LATEST_VERSION = false
            logger.severe("You are running an outdated version of Plugin Portal! We will attempt to update for you.")
            logger.severe("Download Link: https://www.spigotmc.org/resources/plugin-portal.108700")
            logger.severe("Current Version: ${description.version}")
            logger.severe("Latest Version: ${getLatestPPVersion()}")
            val plugin = MarketplacePluginManager.getPlugin(MarketplaceService.SPIGOTMC, 108700)
            install(plugin, URL(plugin.downloadURL))
            return;

        } else {

            Config.init(this)
            Data.init(this)

            val command = PPCommand(this)
            getCommand("pluginportal")!!.setExecutor(command)
            getCommand("pluginportal")!!.tabCompleter = command

            server.pluginManager.registerEvents(PluginValidator(), this)
            server.pluginManager.registerEvents(UpdateListener(this), this)

            setupMetrics(Metrics(this, 18005))
        }
    }

    override fun onDisable() {
        if (!LATEST_VERSION) {
            logger.severe("Please remember to update your Plugin Portal as it is outdated.")
            logger.severe("Download Link: https://www.spigotmc.org/resources/plugin-portal.108700/")
        }
    }
}