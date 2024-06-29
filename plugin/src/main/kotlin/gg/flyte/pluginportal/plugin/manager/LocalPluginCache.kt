package gg.flyte.pluginportal.plugin.manager

import com.google.gson.JsonSyntaxException
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.chat.sendFailure
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.util.*
import gg.flyte.pluginportal.plugin.util.Metrics.registerPluginDownloads
import net.kyori.adventure.audience.Audience
import java.io.File

object LocalPluginCache : PluginCache<LocalPlugin>() {

    fun hasPlugin(plugin: Plugin) = any { local -> plugin.platforms[local.platform]?.id.equals(local.platformId) }
    fun hasPlugin(id: String) = any { it.platformId == id }
    fun hasPlugin(platformId: String, platform: MarketplacePlatform) =
        any { it.platform == platform && it.platformId == platformId }

    fun load() {
        val ppLocalPlugin = LocalPlugin(
            platformId = "5qkQnnWO",
            name = "PluginPortal",
            version = PluginPortal.instance.description.version,
            platform = MarketplacePlatform.MODRINTH,
            sha256 = calculateSHA256(pluginPortalJarFile),
            sha512 = calculateSHA512(pluginPortalJarFile),
            installedAt = System.currentTimeMillis(),
        )

        val text = getPluginsFile().readText()
        if (text.isEmpty()) {
            add(ppLocalPlugin)
            return
        }

        try {
            val plugins = GSON.fromJson(text, Array<LocalPlugin>::class.java)
            plugins.forEach { plugin -> add(plugin) }

            if (any { it.platformId == ppLocalPlugin.platformId }) add(ppLocalPlugin)

            PortalLogger.info(PortalLogger.Action.LOAD_PLUGINS, "Loaded ${plugins.size} plugins from local cache")
        } catch (_: JsonSyntaxException) {
        }
    }

    fun save() {
        val text = GSON.toJson(toTypedArray().distinctBy { plugin -> plugin.platformId })
        getPluginsFile().writeText(text)

        async { registerPluginDownloads() }

        PortalLogger.info(PortalLogger.Action.SAVE_PLUGINS, "Saved $size plugins to local cache")
    }


    fun deletePlugin(plugin: LocalPlugin) {
        val file = plugin.findFile() ?: return
        if (!file.delete()) return

        remove(plugin)
        save()

    }

    fun LocalPlugin.findFile(): File? {
        val pluginsFolder = File("plugins")
        val updateFolder = File(pluginsFolder, "update")

        val files = mutableListOf<File>().apply {
            addAll(pluginsFolder.listFiles() ?: emptyArray())
            addAll(updateFolder.listFiles() ?: emptyArray())
        }

        return files.filter { file -> file.isFile }
            .filter { file -> file.name.endsWith(".jar") }
            .firstOrNull { file -> calculateSHA256(file) == sha256 }
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