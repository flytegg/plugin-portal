package link.portalbox.pluginportal.type

import link.portalbox.pluginportal.PluginPortal
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object Message {
    private lateinit var config: FileConfiguration

    val outdatedPlugin get() = config.getBoolean("outdated-plugin")

    fun init(pluginPortal: PluginPortal) {
        if (Config.language == null) {
            pluginPortal.logger.warning("No language set in config.yml. Defaulting to EN_US")
        }

        val language = Language.valueOf(Config.language?.uppercase() ?: "EN_US")
        val file = File(pluginPortal.dataFolder, "$language.yml")
        if (!file.exists()) {
            pluginPortal.saveResource("$language.yml", true)
        }

        config = YamlConfiguration.loadConfiguration(file)

    }
}