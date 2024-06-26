package gg.flyte.pluginportal.plugin.util

import gg.flyte.pluginportal.plugin.PluginPortal
import org.bukkit.Bukkit

fun isFolia() = runCatching {
    Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
}.isSuccess

fun async(block: () -> Unit) {
    if (isFolia()) {
        block()
    } else {
        Bukkit.getScheduler().runTaskAsynchronously(PluginPortal.instance, block)
    }
}

fun <T> ((T) -> Unit).async(): (T) -> Unit = { t: T -> async { invoke(t) } }

fun delay(ticks: Long, block: () -> Unit) =
    Bukkit.getScheduler().runTaskLater(PluginPortal.instance, block, ticks)