package gg.flyte.pluginportal.plugin.config

import gg.flyte.pluginportal.plugin.PluginPortal
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import java.io.File

object Config {
    private lateinit var config: FileConfiguration

    val INSTALL_DIRECTORY get() = PluginPortal.instance.dataFolder.parentFile
    val UPDATE_DIRECTORY get() = Bukkit.getUpdateFolderFile()

    init {
        PluginPortal.instance.apply {
            if (!PluginPortal.isFreeVersion) {
                saveDefaultConfig()
                this@Config.config = config
            }
        }
    }
}