package gg.flyte.pluginportal.plugin.manager

import com.google.gson.JsonSyntaxException
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.chat.sendFailureMessage
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.util.calculateSHA256
import gg.flyte.pluginportal.plugin.util.createIfNotExists
import net.kyori.adventure.audience.Audience
import java.io.File

object LocalPluginCache : PluginCache<LocalPlugin>() {

    fun hasPlugin(plugin: Plugin) = any { local -> plugin.platforms[local.platform]?.equals(local.platformId) == true }
    fun hasPlugin(id: String) = any { it.platformId == id }
    fun hasPlugin(platformId: String, platform: MarketplacePlatform) = any { it.platform == platform && it.platformId == platformId }

    fun load() {
        val text = getPluginsFile().readText()
        if (text.isEmpty()) return

        try {
            val plugins = GSON.fromJson(text, Array<LocalPlugin>::class.java)
            plugins.forEach { plugin -> add(plugin) }

            PortalLogger.info(PortalLogger.Action.LOAD_PLUGINS, "Loaded ${plugins.size} plugins from local cache")
        } catch (_: JsonSyntaxException) {
        }
    }

    fun save() {
        val text = GSON.toJson(toTypedArray().distinctBy { plugin -> plugin.platformId })
        getPluginsFile().writeText(text)

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
        prefix: String?,
        platformId: String?,
        ifSingle: (LocalPlugin) -> Unit, // Sync
        ifMore: (List<LocalPlugin>) -> Unit // Sync
    ) {
        if (prefix == null && platformId == null) return audience.sendFailureMessage("No plugin name or ID provided")

        val plugins = if (platformId != null) LocalPluginCache.find { it.platformId == platformId }?.let { listOf(it) } ?: listOf()
        else LocalPluginCache.filter { plugin -> plugin.name.startsWith(prefix ?: "", ignoreCase = true) }

        plugins.ifEmpty { return audience.sendFailureMessage("No plugins found") }

        if (plugins.size == 1) ifSingle.invoke(plugins.first())
        else ifMore.invoke(plugins)
    }

}