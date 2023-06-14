package link.portalbox.pluginportal.type

import com.fasterxml.jackson.module.kotlin.readValue
import gg.flyte.pplib.type.version.VersionType
import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.type.language.Message
import gg.flyte.pplib.util.*
import link.portalbox.pluginportal.type.language.Message.serialize
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.FileReader

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

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, installedPlugins)

        pluginPortal.versionType = getLatestVersionType(pluginPortal.description.version)
        if (pluginPortal.versionType != VersionType.LATEST) {
            for (i in 0..2) {
                log(MiniMessage.miniMessage().stripTags(Message.consoleOutdatedPluginPortal.serialize()))
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
        installedPlugins.removeIf { it.marketplacePlugin.id == id }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, installedPlugins)
    }

    private fun archivedInit(pluginPortal: PluginPortal) {
        ymlFile = File(pluginPortal.dataFolder, "data.yml")
        if (!ymlFile.exists()) {
            return
        }

        installedPlugins.addAll(
            objectMapper.readValue<List<LocalPlugin>>(
                convert(
                    ymlFile.inputStream().readBytes().toString(Charsets.UTF_8), pluginPortal.description.version
                )
            )
        )

        //ymlFile.delete()
    }
}