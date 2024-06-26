package gg.flyte.pluginportal.plugin.config

import gg.flyte.pluginportal.plugin.PluginPortal
import org.bukkit.configuration.file.FileConfiguration

object Config {
    private var config: FileConfiguration

    val INSTALL_DIRECTORY get() = config.getString("install-directory")!!
    val UPDATE_DIRECTORY get() = config.getString("update-directory")!!

    init {
        PluginPortal.instance.apply {
            saveDefaultConfig()
            config.options().copyDefaults()
            saveConfig()

            this@Config.config = config
        }

    }
}