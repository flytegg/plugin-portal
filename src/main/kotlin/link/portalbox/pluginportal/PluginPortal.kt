package link.portalbox.pluginportal

import link.portalbox.pluginportal.command.PPCommand
import link.portalbox.pluginportal.file.Config
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.listener.PluginValidator
import link.portalbox.pluginportal.listener.UpdateListener
import link.portalbox.pluginportal.util.setupMetrics
import link.portalbox.pplib.manager.MarketplacePluginManager
import link.portalbox.pplib.manager.MarketplacePluginManager.loadIndex
import link.portalbox.pplib.service.SpigotMCService
import link.portalbox.pplib.type.MarketplaceService
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class PluginPortal : JavaPlugin() {

    var LATEST_VERSION = true

    override fun onEnable() {
        Config.init(this)
        Data.init(this)

        val command = PPCommand(this)
        getCommand("pluginportal")!!.setExecutor(command)
        getCommand("pluginportal")!!.tabCompleter = command

        MarketplacePluginManager.registerService(MarketplaceService.SPIGOTMC, SpigotMCService())
        loadIndex()

        server.pluginManager.registerEvents(PluginValidator(), this)
        server.pluginManager.registerEvents(UpdateListener(this), this)

        setupMetrics(Metrics(this, 18005))
    }

    override fun onDisable() {
        if (!LATEST_VERSION) {
            logger.severe("Please remember to update your Plugin Portal as it is outdated.")
            logger.severe("Download Link: https://www.spigotmc.org/resources/plugin-portal.108700/")
        }
    }

}