package gg.flyte.pluginportal.plugin.util

import gg.flyte.pluginportal.plugin.PluginPortal
import org.bukkit.Bukkit

fun isFolia(): Boolean {
    try {
        Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
        return true
    } catch (e: ClassNotFoundException) {
        return false
    }
}

fun async(block: () -> Unit) {
    if (isFolia()) {
        block()
    } else {
        Bukkit.getScheduler().runTaskAsynchronously(PluginPortal.instance, block)
    }
}

fun delay(ticks: Long, block: () -> Unit) =
    Bukkit.getScheduler().runTaskLater(PluginPortal.instance, block, ticks)