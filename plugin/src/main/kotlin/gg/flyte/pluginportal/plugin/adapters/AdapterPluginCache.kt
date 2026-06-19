package gg.flyte.pluginportal.plugin.adapters

import com.google.gson.JsonSyntaxException
import gg.flyte.pluginportal.common.Constants
import gg.flyte.pluginportal.common.logging.PortalLogger
import gg.flyte.pluginportal.common.managers.PluginCache
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.util.*
import gg.flyte.pluginportal.plugin.PluginPortal
import java.io.File

object AdapterPluginCache : PluginCache<AdapterPlugin>() {

    //    fun hasPlugin(plugin: Plugin) = any { local -> plugin.platforms[local.platform]?.id.equals(local.platformId) }
    fun hasPlugin(id: String) = any { it.platformId == id }
    fun hasPlugin(platformId: String, platform: AdapterPlatform) =
        any { it.platform == platform && it.platformId == platformId }

    fun hasPluginByHash(sha256: String) = any { it.sha256 == sha256 }

    fun fromPlugin(plugin: Plugin) = find { plugin.platforms.asList().any { mp -> mp.platformId == it.platformId } }

    fun load() {
        val pluginsInFolder: Map<String, File> = Constants.INSTALL_DIRECTORY
            .listFiles()
            ?.filter(File::isJarFile)
            ?.associateBy { HashType.SHA256.hash(it) }
            ?: mapOf()

        val text = getPluginsFile().readText()
        try {
            val plugins = GSON.fromJson(text, Array<AdapterPlugin>::class.java) ?: emptyArray()
            var loaded = 0
            plugins.forEach { plugin ->
//                if (plugin.isPluginPortal) {
//                    add(pppLocal)
                    // TODO: don't think anything needs to be here anymore.
//                }

                if (!plugin.hasValidPlatform()) {
                    PortalLogger.warn("Skipped ${plugin.name} (${plugin.platformId}) from adapter cache because it has an unknown adapter platform.")
                } else if (!pluginsInFolder.containsKey(plugin.sha256)) {
                    // Plugin no longer present in plugins folder upon opening server
                    PortalLogger.info(PortalLogger.Action.NOTICED_DELETE,"of ${plugin.name} (${plugin.platformId}) while Plugin Portal was Disabled.")
                } else {
                    add(plugin)
                    loaded++
                }
            }

            // Can just add this back if ever needed.
//            if (none(AdapterPlugin::isPluginPortal)) add(pppLocal)

            save()

            PortalLogger.info(PortalLogger.Action.LOAD_PLUGINS, "Loaded $loaded plugins from adapter cache")
        } catch (_: JsonSyntaxException) {
        }
    }

    fun save() {
        async {
            val text = GSON.toJson(toTypedArray().distinctBy { plugin -> plugin.platformId })
            getPluginsFile().writeText(text)
        }
    }


    fun deletePlugin(plugin: AdapterPlugin, toDelete: List<File?>) {
        remove(plugin)
        save()
        toDelete.forEach { it?.delete() }
    }

    private val updatedPluginMap: HashMap<AdapterPlugin, File?> = hashMapOf()
    fun addToUpdatedPluginMap(newPlugin: AdapterPlugin, oldPlugin: AdapterPlugin) {
        updatedPluginMap[newPlugin] = oldPlugin.findFile()
    }

    /**
     * @return The file for the currently installed version of this plugin, if this [LocalPlugin] was installed via an update,
     *             otherwise null
     */
    fun AdapterPlugin.popCurrentVersionFile() = updatedPluginMap.remove(this)

    private val pluginsFolder = File("plugins")
    private val updateFolder = File(pluginsFolder, "update").apply { if (!exists()) mkdirs() }

    private fun AdapterPlugin.hasValidPlatform() = runCatching { platform.name }.isSuccess

    fun AdapterPlugin.findFile(): File? {
        val files = mutableListOf<File>().apply {
            addAll(pluginsFolder.listFiles() ?: emptyArray())
            addAll(updateFolder.listFiles() ?: emptyArray())
        }

        return files.filter { file -> file.isFile }
            .filter { file -> file.name.endsWith(".jar") }
            .firstOrNull { file -> HashType.SHA256.hash(file) == sha256 }
    }


    private fun getPluginsFile() = File(PluginPortal.instance.dataFolder, "adapter-plugins.json").createIfNotExists()
}
