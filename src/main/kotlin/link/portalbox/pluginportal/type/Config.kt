package link.portalbox.pluginportal.type

import link.portalbox.pluginportal.PluginPortal
import gg.flyte.pplib.type.Service
import org.bukkit.configuration.file.FileConfiguration

object Config {
    private lateinit var config: FileConfiguration

    val startupOnInstall get() = config.getBoolean("startup-on-install")
    val marketplaceService get() = config.getString("marketplace-service")
        ?.let { Service.valueOf(it.uppercase()) }
    val language get() = config.getString("language")

    fun init(pluginPortal: PluginPortal) {
        pluginPortal.saveDefaultConfig()
        config = pluginPortal.config
        config.options().copyDefaults(true)
        pluginPortal.saveConfig()
    }
}