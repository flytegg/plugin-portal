package gg.flyte.pluginportal.plugin.manager

import com.google.gson.JsonSyntaxException
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.util.createIfNotExists
import org.bukkit.Bukkit
import java.io.File

object LocalPluginCache : PluginCache<LocalPlugin>() {

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
        val text = GSON.toJson(toTypedArray())
        getPluginsFile().writeText(text)

        println(getPluginsFile().absolutePath)

        PortalLogger.info(PortalLogger.Action.SAVE_PLUGINS, "Saved $size plugins to local cache")
    }

    private fun getPluginsFile() = File(PluginPortal.instance.dataFolder, "plugins.json").createIfNotExists()

}