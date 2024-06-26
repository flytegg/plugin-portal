package gg.flyte.pluginportal.plugin.config

import gg.flyte.pluginportal.plugin.PluginPortal
import org.bukkit.configuration.file.FileConfiguration

object Config {
    private var config: FileConfiguration

    val INSTALL_DIRECTORY get() = config.getString("install-directory")!!
    val UPDATE_DIRECTORY get() = config.getString("update-directory")!!

    // There is no way this works... Just load based upon index in the future.
    val PLATFORM_PRIORITY: LinkedHashSet<String> get() = config.getStringList("platform-priority")
        .toCollection(LinkedHashSet())

    init {
        PluginPortal.instance.apply {
            saveDefaultConfig()
            config.options().copyDefaults()
            saveConfig()

            this@Config.config = config
        }

    }
}