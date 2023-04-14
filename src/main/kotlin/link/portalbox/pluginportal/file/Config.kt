package link.portalbox.pluginportal.file

import link.portalbox.pluginportal.PluginPortal
import org.bukkit.configuration.file.FileConfiguration

object Config {
    private lateinit var config: FileConfiguration

    val startupOnInstall get() = config.getBoolean("startup-on-install")

    fun init(pluginPortal: PluginPortal) {
        pluginPortal.saveDefaultConfig()
        config = pluginPortal.config
    }
}