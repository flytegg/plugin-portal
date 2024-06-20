package gg.flyte.pluginportal.plugin.manager

import com.google.gson.JsonSyntaxException
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.util.calculateSHA256
import gg.flyte.pluginportal.plugin.util.createIfNotExists
import java.io.File

object LocalPluginCache : PluginCache<LocalPlugin>() {

    fun hasPlugin(plugin: Plugin) = hasPlugin(plugin.id)
    fun hasPlugin(id: String) = any { it.id == id }

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
        val text = GSON.toJson(toTypedArray().distinctBy { plugin -> plugin.id })
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

}