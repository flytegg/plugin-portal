package link.portalbox.pluginportal

import link.portalbox.pluginportal.command.PPCommand
import link.portalbox.pluginportal.type.Config
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.listener.PluginValidator
import link.portalbox.pluginportal.listener.UpdateListener
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.util.*
import gg.flyte.pplib.manager.MarketplacePluginManager
import gg.flyte.pplib.service.HangarService
import gg.flyte.pplib.service.SpigotMCService
import gg.flyte.pplib.type.MarketplaceService
import gg.flyte.pplib.type.VersionType
import gg.flyte.pplib.util.BASE_DOMAIN
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class PluginPortal : JavaPlugin() {
    var versionType: VersionType = VersionType.UNRELEASED

    override fun onEnable() {
        BASE_DOMAIN = "http://localhost:5005"

        Config.init(this)
        Message.init(this)
        Data.init(this)

        MarketplacePluginManager.registerService(MarketplaceService.SPIGOTMC, SpigotMCService())
        MarketplacePluginManager.registerService(MarketplaceService.HANGAR, HangarService())

        val command = PPCommand(this)
        getCommand("pluginportal")!!.setExecutor(command)
        getCommand("pluginportal")!!.tabCompleter = command

        server.pluginManager.registerEvents(PluginValidator(), this)
        server.pluginManager.registerEvents(UpdateListener(this), this)

        setupMetrics(Metrics(this, 18005))
    }
}