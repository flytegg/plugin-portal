package link.portalbox.pluginportal.file

import link.portalbox.pluginportal.PluginPortal
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object Config {
    private lateinit var config: FileConfiguration

    val startupOnInstall get() = config.getBoolean("startup-on-install")

    fun init(pluginPortal: PluginPortal) {
        if (!(pluginPortal.dataFolder.parentFile.listFiles()?.contains(File("PluginPortal")))!!) {
            var folder = File(pluginPortal.dataFolder.parentFile, "PluginPortal")
            folder.mkdir()
            pluginPortal.saveResource("config.yml", true)
        }

        config = YamlConfiguration.loadConfiguration(File(pluginPortal.dataFolder.parentFile, "PluginPortal"))
    }
}