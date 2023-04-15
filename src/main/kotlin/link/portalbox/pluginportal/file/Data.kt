package link.portalbox.pluginportal.file

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.listener.UpdateListener
import link.portalbox.pplib.type.VersionType
import link.portalbox.pplib.util.getLatestVersion
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object Data {
    private lateinit var file: File
    private lateinit var config: YamlConfiguration

    var installedPlugins = mutableListOf<LocalPlugin>()

    fun init(pluginPortal: PluginPortal) {
        file = File(pluginPortal.dataFolder, "data.yml")
        if (!file.exists()) {
            file.createNewFile()
        }
        config = YamlConfiguration.loadConfiguration(file)

        config.getKeys(false).forEach { id ->
            val pluginSection = config.getConfigurationSection(id)
            if (pluginSection != null) {
                installedPlugins.add(LocalPlugin(id.toInt(), pluginSection.getString("version")!!, pluginSection.getString("file")!!))
            }
        }

        pluginPortal.versionType = getLatestVersion(pluginPortal.description.version)
        if (pluginPortal.versionType != VersionType.LATEST) {
            for (i in 0..2) {
                pluginPortal.logger.info("Plugin Portal is outdated. Download the new version here: https://www.spigotmc.org/resources/plugin-portal.108700/")
            }

        }

    }

    fun update(id: Int, version: String, fileSha: String) {
        config.set("${id}.version", version)
        config.set("${id}.file", fileSha)
        config.save(file)

        val plugin = installedPlugins.find { it.id == id }
        if (plugin != null) {
            plugin.version = version
            plugin.fileSha = fileSha
        } else {
            installedPlugins.add(LocalPlugin(id, version, fileSha))
        }
    }

    fun delete(id: Int) {
        config.set(id.toString(), null)
        config.save(file)

        installedPlugins.removeIf { it.id == id }
    }
}