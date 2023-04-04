package link.portalbox.pluginportal.util

import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie


var metrics: Metrics? = null;

fun setupMetrics(bMetrics: Metrics) {
    metrics = bMetrics
}

fun addValueToPieChart(chartType: Chart, value: String) {
    metrics?.addCustomChart(SimplePie(chartType.id) { value })
}

enum class Chart(val id: String) {
    MOSTDOWNLOADED("most_downloaded"),
    MOSTINVALIDDOWNLOADS("most_invalid_downloads");
}
