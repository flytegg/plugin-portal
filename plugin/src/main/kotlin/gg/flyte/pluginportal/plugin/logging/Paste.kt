package gg.flyte.pluginportal.plugin.logging

import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gs.mclo.api.MclogsClient
import java.nio.file.Paths

object Paste {
    private val pasteClient = MclogsClient("Plugin-Portal", PluginPortal.instance.description.version, PluginPortal.instance.server.version)

    private fun header(): String {
        return """
            |----------------------------------------------------------------
            |Plugin Portal Log Dump
            |Server: ${PluginPortal.instance.server.name} - ${PluginPortal.instance.server.version}
            |Plugins Installed (PP/Server): ${LocalPluginCache.size}/${PluginPortal.instance.server.pluginManager.plugins.size}
            |Plugin Portal Version: ${PluginPortal.instance.description.version}
            |----------------------------------------------------------------
            
            
            
            """.trimIndent()
    }

    fun dump(): String {
        var log = header()
        log += Paths.get("./logs/latest.log").toFile().readText()

        val response = pasteClient.uploadLog(log).get()
        PluginPortal.instance.logger.info("Log dumped to ${response.url}")

        return response.url
    }
}