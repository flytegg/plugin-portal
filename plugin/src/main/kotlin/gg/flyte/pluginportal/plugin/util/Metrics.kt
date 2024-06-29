package gg.flyte.pluginportal.plugin.util

import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import org.bstats.bukkit.Metrics
import org.bstats.charts.AdvancedPie

object Metrics {

    private var metrics: Metrics = Metrics(PluginPortal.instance, 18005)

    fun registerPluginDownloads() {
        metrics.addCustomChart(AdvancedPie("plugins_downloaded") {
            val plugins = HashMap<String, Int>()

            LocalPluginCache.forEach { plugin ->
                plugins[plugin.name] = 1
            }

            plugins
        })
    }

}