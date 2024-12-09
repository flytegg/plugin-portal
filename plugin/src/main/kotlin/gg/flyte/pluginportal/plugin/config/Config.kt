package gg.flyte.pluginportal.plugin.config

import gg.flyte.pluginportal.plugin.PluginPortal
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration

object Config {
    private var config: FileConfiguration

    val INSTALL_DIRECTORY get() = PluginPortal.instance.dataFolder.parentFile
    val UPDATE_DIRECTORY get() = Bukkit.getUpdateFolderFile()

    init {
        PluginPortal.instance.apply {
            saveDefaultConfig()
            config.options().copyDefaults()
            saveConfig()

            this@Config.config = config
        }

    }
}