package link.portalbox.pluginportal.type

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.type.language.Message
import gg.flyte.pplib.type.VersionType
import gg.flyte.pplib.util.getLatestVersion
import org.bukkit.Bukkit
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

        // Create a new YamlConfiguration object with updated keys
        val updatedConfig = YamlConfiguration()

        for (key in config.getKeys(false)) {
            val value = config.getConfigurationSection(key)

            // Check if the key is in the old format
            val oldKey = key.split(":")
            if (oldKey.size == 1) {
                println("Updating key $key to new format")
                // Update the key to the new format
                val newKey = "SPIGOTMC:${oldKey[0]}"
                updatedConfig.set(newKey, value)
            } else {
                // Use the key as is
                updatedConfig.set(key, value)
            }
        }

        // Save the updated YAML file
        updatedConfig.save(file)
        config = updatedConfig

        updatedConfig.getKeys(false).forEach { id ->
            val pluginSection = updatedConfig.getConfigurationSection(id)
            if (pluginSection != null) {
                installedPlugins.add(LocalPlugin(id, pluginSection.getString("version")!!, pluginSection.getString("file")!!))
            }
        }

        println(pluginPortal.description.version)
        pluginPortal.versionType = getLatestVersion(pluginPortal.description.version)
        if (pluginPortal.versionType != VersionType.LATEST) {
            for (i in 0..2) {
                Bukkit.getConsoleSender().sendMessage(Message.consoleOutdatedPluginPortal)
            }
        }
    }

    fun update(id: String, version: String, fileSha: String) {
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

    fun delete(id: String) {
        config.set(id, null)
        config.save(file)

        installedPlugins.removeIf { it.id == id }
    }
}