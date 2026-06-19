package gg.flyte.pluginportal.common.util

import gg.flyte.pluginportal.common.PluginPortalBase
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

private val plugin get() = PluginPortalBase.plugin

fun isFolia() = runCatching {
    Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
}.isSuccess

fun async(block: () -> Unit) {
    if (isFolia()) {
        block()
    } else {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, block)
    }
}

class CancellableTimer<T>(private val scheduler: T, private val cancel: (T) -> Unit) {
    fun cancel() = cancel.invoke(scheduler)
}

fun asyncTimer(intervalTicks: Int, delayTicks: Int, block: () -> Unit): CancellableTimer<*> {
    return if (isFolia()) {
        CancellableTimer<ScheduledFuture<*>>(Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(block, delayTicks * 50L, intervalTicks * 50L, TimeUnit.MILLISECONDS)) {
            it.cancel(false)
        }
    } else {
        CancellableTimer<BukkitTask>(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, block, delayTicks.toLong(), intervalTicks.toLong())) {
            it.cancel()
        }
    }
}

fun <T> ((T) -> Unit).async(): (T) -> Unit = { t: T -> async { invoke(t) } }

fun delay(ticks: Long, block: () -> Unit) =
    Bukkit.getScheduler().runTaskLater(plugin, block, ticks)

fun delay(ticks: Long, async: Boolean, block: () -> Unit) =
    if (async) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, block, ticks)
    } else {
        Bukkit.getScheduler().runTaskLater(plugin, block, ticks)
    }