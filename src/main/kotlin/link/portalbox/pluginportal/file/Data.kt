package link.portalbox.pluginportal.file

import link.portalbox.pluginportal.PluginPortal
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object Data {

    private lateinit var file: File
    private lateinit var config: YamlConfiguration

    fun init(pluginPortal: PluginPortal) {
        file = File(pluginPortal.dataFolder, "data.yml");
        if (!file.exists()) {
            file.createNewFile()
        }
        config = YamlConfiguration.loadConfiguration(file)
    }

    val installedPlugins: List<LocalPlugin>
        get() {
            val pluginIds = config.getKeys(false)
            return pluginIds.mapNotNull { id ->
                val pluginSection = config.getConfigurationSection("plugins.$id") ?: return@mapNotNull null
                val version = pluginSection.getString("version") ?: return@mapNotNull null
                val fileSha = pluginSection.getString("fileSha") ?: return@mapNotNull null
                LocalPlugin(id.toInt(), version, fileSha)
            }
        }

    fun getPlugin(id: Int): LocalPlugin? {
        return if (config.isConfigurationSection(id.toString())) {
            null
        } else {
            LocalPlugin(id, config.getString("${id}.version")!!, config.getString("${id}.file")!!)
        }
    }



    fun update(id: Int, version: String, fileSha: String) {
        config.set("${id}.version", version)
        config.set("${id}.file", fileSha)
        config.save(file)
    }

}