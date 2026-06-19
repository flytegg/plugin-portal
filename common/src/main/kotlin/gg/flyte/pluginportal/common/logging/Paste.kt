package gg.flyte.pluginportal.common.logging

import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gs.mclo.api.MclogsClient
import java.nio.file.Paths

object Paste {
    private val plugin get() = PluginPortalBase.plugin
    private val pasteClient = MclogsClient("Plugin-Portal", plugin.description.version, plugin.server.version)

    private fun header(): String {
        return """
            |----------------------------------------------------------------
            |Plugin Portal Log Dump
            |Server: ${plugin.server.name} - ${plugin.server.version}
            |Plugins Installed (PP/Server): ${LocalPluginCache.size}/${plugin.server.pluginManager.plugins.size}
            |Plugin Portal Version: ${plugin.description.version}
            |----------------------------------------------------------------
            
            
            
            """.trimIndent()
    }

    fun dump(): String {
        var log = header()
        log += Paths.get("./logs/latest.log").toFile().readText()

        val response = pasteClient.uploadLog(log).get()
        plugin.logger.info("Log dumped to ${response.url}")

        return response.url
    }

    fun upload(text: String): String {
        val response = pasteClient.uploadLog(text).get()
        plugin.logger.info("Log uploaded to ${response.url}")

        return response.url
    }

    fun getRawContent(id: String): String {
        val response = pasteClient.getRawLogContent(id).get()
        plugin.logger.info("Raw content fetched from $id")

        return response
    }
}