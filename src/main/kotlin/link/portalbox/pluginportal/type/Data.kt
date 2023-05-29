package link.portalbox.pluginportal.type

import gg.flyte.pplib.type.Service
import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.type.language.Message
import gg.flyte.pplib.type.VersionType
import gg.flyte.pplib.util.*
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object Data {
    private lateinit var ymlFile: File
    private lateinit var file: File
    private lateinit var config: YamlConfiguration

    val installedPlugins = HashSet<LocalPlugin>()

    fun init(pluginPortal: PluginPortal) {
        archivedInit(pluginPortal)

        file = File(pluginPortal.dataFolder, "PluginData.json")
        if (!file.exists()) {
            file.createNewFile()
        } else {
            runCatching {
                objectMapper.readValue<List<LocalPlugin>>(
                    file,
                    objectMapper.typeFactory.constructCollectionType(HashSet::class.java, LocalPlugin::class.java)
                ).forEach {
                    installedPlugins.add(it)
                }
            }
        }

        pluginPortal.versionType = getLatestVersion(pluginPortal.pluginMeta.version)
        if (pluginPortal.versionType != VersionType.LATEST) {
            for (i in 0..2) {
                Bukkit.getConsoleSender().sendMessage(Message.consoleOutdatedPluginPortal)
            }
        }
    }

    fun update(localPlugin: LocalPlugin) {
        val existingLocalPlugin = installedPlugins.find { it.marketplacePlugin.id == localPlugin.marketplacePlugin.id }
        if (existingLocalPlugin != null) {
            existingLocalPlugin.marketplacePlugin.version = localPlugin.marketplacePlugin.version
            existingLocalPlugin.fileSha = localPlugin.fileSha
        } else {
            installedPlugins.add(localPlugin)
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, installedPlugins)
    }

    fun delete(id: String) {
        installedPlugins.removeIf { it.id == id }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, installedPlugins)
    }

    private fun archivedInit(pluginPortal: PluginPortal) {
        ymlFile = File(pluginPortal.dataFolder, "data.yml")
        if (!ymlFile.exists()) {
            return
        }

        config = YamlConfiguration.loadConfiguration(ymlFile)
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
        updatedConfig.save(ymlFile)
        config = updatedConfig

        updatedConfig.getKeys(false).forEach { id ->
            val pluginSection = updatedConfig.getConfigurationSection(id)
            if (pluginSection != null) {
                val separated = separateServiceAndName(id)
                println("Adding $id")
                if (separated.first == Service.SPIGOTMC) {
                    Bukkit.getScheduler().runTaskAsynchronously(pluginPortal, Runnable {
                        installedPlugins.add(
                            LocalPlugin(
                                separated.second,
                                getPluginFromId(separated.second)?.name ?: separated.second,
                                separated.first,
                                pluginSection.getString("version")!!,
                                pluginSection.getString("file")!!
                            )
                        )
                    })
                } else {
                    installedPlugins.add(
                        LocalPlugin(
                            separated.second,
                            separated.second,
                            separated.first,
                            pluginSection.getString("version")!!,
                            pluginSection.getString("file")!!
                        )
                    )
                }

            }
        }

        //ymlFile.delete()
    }
}