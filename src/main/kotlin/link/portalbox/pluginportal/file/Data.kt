package link.portalbox.pluginportal.file

import link.portalbox.pluginportal.PluginPortal
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object Data {
    private lateinit var file: File
    private lateinit var config: YamlConfiguration

    val installedPlugins: HashSet<LocalPlugin> = HashSet()

    fun init(pluginPortal: PluginPortal) {
        for (file in pluginPortal.dataFolder.parentFile.listFiles()!!) {
            if (!file.name.lowercase().contains("pluginportal")) return
            if (!file.isDirectory) return

            runCatching {
                val dataFile = YamlConfiguration.loadConfiguration(File(file, "data.yml"))
                dataFile.getKeys(false).forEach { id ->
                    val pluginSection = dataFile.getConfigurationSection(id)
                    if (pluginSection != null) {
                        installedPlugins.add(
                            LocalPlugin(
                                id.toInt(),
                                pluginSection.getString("version")!!,
                                pluginSection.getString("file")!!
                            )
                        )
                    }
                }

                println("${file.name} is an old plugin folder!")
            }

        }

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