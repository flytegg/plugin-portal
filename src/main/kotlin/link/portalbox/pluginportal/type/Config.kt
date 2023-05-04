package link.portalbox.pluginportal.type

import link.portalbox.pluginportal.PluginPortal
import org.bukkit.configuration.file.FileConfiguration

object Config {
    private lateinit var config: FileConfiguration

    val startupOnInstall get() = config.getBoolean("startup-on-install")
    val cacheTime get() = config.getLong("cache-time-in-minutes")

    fun init(pluginPortal: PluginPortal) {
        pluginPortal.saveDefaultConfig()
        config = pluginPortal.config
    }
}