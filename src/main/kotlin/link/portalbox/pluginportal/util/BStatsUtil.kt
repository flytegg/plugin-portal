package link.portalbox.pluginportal.util

import org.bstats.bukkit.Metrics

var metrics: Metrics? = null

fun setupMetrics(bMetrics: Metrics) {
    metrics = bMetrics
}