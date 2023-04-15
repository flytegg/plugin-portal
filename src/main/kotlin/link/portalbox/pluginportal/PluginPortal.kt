package link.portalbox.pluginportal

import link.portalbox.pluginportal.command.PPCommand
import link.portalbox.pluginportal.file.Config
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.listener.PluginValidator
import link.portalbox.pluginportal.listener.UpdateListener
import link.portalbox.pluginportal.util.*
import link.portalbox.pplib.main
import link.portalbox.pplib.manager.MarketplacePluginManager
import link.portalbox.pplib.manager.MarketplacePluginManager.loadIndex
import link.portalbox.pplib.service.SpigotMCService
import link.portalbox.pplib.type.MarketplaceService
import link.portalbox.pplib.type.VersionType
import link.portalbox.pplib.util.getPPVersions
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin
import java.net.URL
import javax.management.modelmbean.ModelMBeanAttributeInfo

class PluginPortal : JavaPlugin() {
    var versionType: VersionType = VersionType.UNRELEASED

    override fun onEnable() {
        Config.init(this)
        Data.init(this)

        MarketplacePluginManager.registerService(MarketplaceService.SPIGOTMC, SpigotMCService())
        loadIndex()

        val command = PPCommand(this)
        getCommand("pluginportal")!!.setExecutor(command)
        getCommand("pluginportal")!!.tabCompleter = command

        server.pluginManager.registerEvents(PluginValidator(), this)
        server.pluginManager.registerEvents(UpdateListener(this), this)

        setupMetrics(Metrics(this, 18005))

    }
}