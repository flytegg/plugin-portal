package gg.flyte.pluginportal.bukkit.manager

import gg.flyte.pluginportal.bukkit.PluginPortal
import org.bukkit.configuration.file.FileConfiguration
import java.io.File

object Config {
    private lateinit var config: FileConfiguration

    val language get() = config.getString("language")

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