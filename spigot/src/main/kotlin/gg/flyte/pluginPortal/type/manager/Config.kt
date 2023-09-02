package gg.flyte.pluginPortal.type.manager

import gg.flyte.pluginPortal.PluginPortal
import org.bukkit.configuration.file.FileConfiguration
import java.io.File

object Config {
    private lateinit var config: FileConfiguration

    val startupOnInstall get() = config.getBoolean("startup-on-install")
    val marketplaceService get() = config.getString("marketplace-service")?.uppercase()
    val language get() = config.getString("language")
    val hangarUsername get() = config.getString("hangar-username") ?: "Username"

    fun init(pluginPortal: PluginPortal) {
        pluginPortal.saveDefaultConfig()
        config = pluginPortal.config
        config.options().copyDefaults(true)
        pluginPortal.saveConfig()

        File(pluginPortal.dataFolder, "plugins.json").apply {
           if (!exists()) {
               createNewFile()
               writeText("[]")
           }
        }
    }
}