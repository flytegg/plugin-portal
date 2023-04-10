package link.portalbox.pluginportal

import link.portalbox.pluginportal.command.PPCommand
import link.portalbox.pluginportal.file.Config
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.listener.PluginValidator
import link.portalbox.pluginportal.listener.UpdateListener
import link.portalbox.pluginportal.util.*
import link.portalbox.pplib.manager.MarketplacePluginManager
import link.portalbox.pplib.manager.MarketplacePluginManager.loadIndex
import link.portalbox.pplib.service.SpigotMCService
import link.portalbox.pplib.type.MarketplaceService
import link.portalbox.pplib.util.getLatestPPVersion
import link.portalbox.pplib.util.getPPVersions
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin
import java.net.URL

class PluginPortal : JavaPlugin() {
    var LATEST_VERSION = true

    override fun onEnable() {
        Config.init(this)
        Data.init(this)

        MarketplacePluginManager.registerService(MarketplaceService.SPIGOTMC, SpigotMCService())
        loadIndex()

        if (!isLatestVersion(this)) {
            LATEST_VERSION = false
            logger.info("Plugin Portal outdated, installing new version...")
            val plugin = MarketplacePluginManager.getPlugin(MarketplaceService.SPIGOTMC, 108700)
            val sha256s = getPPVersions()?.values
            if (!dataFolder.parentFile.listFiles()?.any { getSHA(it) in sha256s!! }!!) {
                install(plugin, URL(plugin.downloadURL), true)
            }

            server.pluginManager.disablePlugin(this)
            return;

        } else {
            val command = PPCommand(this)
            getCommand("pluginportal")!!.setExecutor(command)
            getCommand("pluginportal")!!.tabCompleter = command

            server.pluginManager.registerEvents(PluginValidator(), this)
            server.pluginManager.registerEvents(UpdateListener(this), this)

            setupMetrics(Metrics(this, 18005))
            deleteOutdatedPP(this)
        }
    }
}