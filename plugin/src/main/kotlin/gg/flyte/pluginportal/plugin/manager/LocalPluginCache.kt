package gg.flyte.pluginportal.plugin.manager

import com.google.gson.JsonSyntaxException
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.PluginPortal.Companion.pluginPortalJarFile
import gg.flyte.pluginportal.plugin.chat.sendFailure
import gg.flyte.pluginportal.plugin.config.Config
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.util.*
import net.kyori.adventure.audience.Audience
import java.io.File

object LocalPluginCache : PluginCache<LocalPlugin>() {

    fun hasPlugin(plugin: Plugin) = any { local -> plugin.platforms[local.platform]?.id.equals(local.platformId) }
    fun hasPlugin(id: String) = any { it.platformId == id }
    fun hasPlugin(platformId: String, platform: MarketplacePlatform) =
        any { it.platform == platform && it.platformId == platformId }

    fun fromPlugin(plugin: Plugin) = find { plugin.platforms.values.any { mp -> mp.id == it.platformId } }

    fun load() {
        val ppLocalPlugin = LocalPlugin(
            platformId = "5qkQnnWO",
            name = "PluginPortal",
            version = PluginPortal.instance.description.version,
            platform = MarketplacePlatform.MODRINTH,
            sha256 = HashType.SHA256.hash(pluginPortalJarFile),
            sha512 = HashType.SHA512.hash(pluginPortalJarFile),
            installedAt = System.currentTimeMillis(),
        )

        val pluginsInFolder: Map<String, File> = Config.INSTALL_DIRECTORY
            .listFiles()
            ?.filter(File::isJarFile)
            ?.associateBy { HashType.SHA256.hash(it) }
            ?: mapOf()

        val text = getPluginsFile().readText()
        if (text.isEmpty()) {
            add(ppLocalPlugin)
            return
        }

        try {
            val plugins = GSON.fromJson(text, Array<LocalPlugin>::class.java)
            plugins.forEach { plugin ->
                if (plugin.isPluginPortal) {
                    add(ppLocalPlugin) // Update PP
                } else if (!pluginsInFolder.containsKey(plugin.sha256)) {
                    // Plugin no longer present in plugins folder upon opening server
                    PortalLogger.info(
                        PortalLogger.Action.NOTICED_DELETE,
                        "of ${plugin.name} (${plugin.platformId}) while Plugin Portal was Disabled."
                    )
                } else {
                    add(plugin)
                }
            }

            if (none(LocalPlugin::isPluginPortal)) add(ppLocalPlugin)

            save()

            PortalLogger.info(PortalLogger.Action.LOAD_PLUGINS, "Loaded ${plugins.size} plugins from local cache")
        } catch (_: JsonSyntaxException) {
        }
    }

    fun save() {
        async {
            val text = GSON.toJson(toTypedArray().distinctBy { plugin -> plugin.platformId })
            getPluginsFile().writeText(text)
        }
    }


    fun deletePlugin(plugin: LocalPlugin, toDelete: List<File?>) {
        remove(plugin)
        save()
        toDelete.forEach { it?.delete() }
    }

    private val updatedPluginMap: HashMap<LocalPlugin, File?> = hashMapOf()
    fun addToUpdatedPluginMap(newPlugin: LocalPlugin, oldPlugin: LocalPlugin) { updatedPluginMap[newPlugin] = oldPlugin.findFile() }
    /**
     * @return The file for the currently installed version of this plugin, if this [LocalPlugin] was installed via an update,
     *             otherwise null
     */
    fun LocalPlugin.popCurrentVersionFile() = updatedPluginMap.remove(this)

    private val pluginsFolder = File("plugins")
    private val updateFolder = File(pluginsFolder, "update").apply { if (!exists()) mkdirs() }

    fun LocalPlugin.findFile(): File? {
        val files = mutableListOf<File>().apply {
            addAll(pluginsFolder.listFiles() ?: emptyArray())
            addAll(updateFolder.listFiles() ?: emptyArray())
        }

        return files.filter { file -> file.isFile }
            .filter { file -> file.name.endsWith(".jar") }
            .firstOrNull { file -> HashType.SHA256.hash(file) == sha256 }
    }


    private fun getPluginsFile() = File(PluginPortal.instance.dataFolder, "plugins.json").createIfNotExists()

    fun searchPluginsWithFeedback(
        audience: Audience,
        name: String,
        nameIsId: Boolean,
        ifSingle: (LocalPlugin) -> Unit, // Sync
        ifMore: (List<LocalPlugin>) -> Unit // Sync
    ) {
        val prefix = if (nameIsId) null else name
        val platformId = if (nameIsId) name else null

        val plugins = if (platformId != null) LocalPluginCache.find { it.platformId == platformId }?.let { listOf(it) }
            ?: listOf()
        else LocalPluginCache.filter { plugin -> plugin.name.startsWith(prefix ?: "", ignoreCase = true) }

        plugins.ifEmpty { return audience.sendFailure("No plugins found") }

        if (plugins.size == 1) ifSingle.invoke(plugins.first())
        else ifMore.invoke(plugins)
    }
}
