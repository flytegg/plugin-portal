package link.portalbox.pluginportal

import link.portalbox.pluginportal.command.PPCommand
import link.portalbox.pluginportal.type.Config
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.listener.PluginValidator
import link.portalbox.pluginportal.listener.UpdateListener
import link.portalbox.pluginportal.util.*
import link.portalbox.pplib.manager.MarketplacePluginManager
import link.portalbox.pplib.manager.MarketplacePluginManager.loadIndex
import link.portalbox.pplib.service.HangarService
import link.portalbox.pplib.service.SpigotMCService
import link.portalbox.pplib.type.MarketplaceService
import link.portalbox.pplib.type.VersionType
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class PluginPortal : JavaPlugin() {
    var versionType: VersionType = VersionType.UNRELEASED

    override fun onEnable() {
        Config.init(this)
        Data.init(this)

        MarketplacePluginManager.registerService(MarketplaceService.SPIGOTMC, SpigotMCService())
        MarketplacePluginManager.registerService(MarketplaceService.HANGAR, HangarService())
        startCacheTask(this)

        val command = PPCommand(this)
        getCommand("pluginportal")!!.setExecutor(command)
        getCommand("pluginportal")!!.tabCompleter = command

        server.pluginManager.registerEvents(PluginValidator(), this)
        server.pluginManager.registerEvents(UpdateListener(this), this)

        setupMetrics(Metrics(this, 18005))

        Math.abs(-1)
    }
}