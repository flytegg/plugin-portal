package link.portalbox.pluginportal

import link.portalbox.pluginportal.command.PPCommand
import link.portalbox.pluginportal.file.Config
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.listener.PluginValidator
import link.portalbox.pluginportal.listener.UpdateListener
import link.portalbox.pluginportal.util.setupMetrics
import link.portalbox.pplib.manager.MarketplaceManager
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

        MarketplaceManager()

        server.pluginManager.registerEvents(UpdateListener(this), this)
        server.pluginManager.registerEvents(PluginValidator(), this)
    }

    override fun onDisable() {
        if (!LATEST_VERSION) {
            logger.severe("Please remember to update your Plugin Portal as it is outdated.")
            logger.severe("Download Link: https://www.spigotmc.org/resources/plugin-portal.108700/")
        }
    }

}