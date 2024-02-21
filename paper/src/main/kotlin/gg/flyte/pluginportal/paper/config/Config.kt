package gg.flyte.pluginportal.paper.config

import gg.flyte.pluginportal.paper.PluginPortal
import org.bukkit.configuration.file.FileConfiguration
import java.io.File

object Config {
    private var config: FileConfiguration

    val language get() = config.getString("language") ?: "EN_US"

    init {
        PluginPortal.instance.apply {
            saveDefaultConfig()
            this@Config.config = config

            File(dataFolder, "plugins.json").apply {
                if (!exists()) {
                    createNewFile()
                    writeText("[]")
                }
            }
        }
    }
}