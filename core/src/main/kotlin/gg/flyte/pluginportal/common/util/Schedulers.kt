package gg.flyte.pluginportal.common.util

import gg.flyte.pluginportal.common.PluginPortalBase
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

// These tasks perform file, HTTP and chat work; they never access world state.
fun async(block: () -> Unit) { PluginPortalBase.plugin.executor.execute(block) }

class CancellableTimer<T>(private val scheduler: T, private val cancel: (T) -> Unit) {
    fun cancel() = cancel.invoke(scheduler)
}

fun asyncTimer(intervalTicks: Int, delayTicks: Int, block: () -> Unit): CancellableTimer<*> =
    CancellableTimer<ScheduledFuture<*>>(PluginPortalBase.plugin.executor.scheduleAtFixedRate(
        block, delayTicks * 50L, intervalTicks * 50L, TimeUnit.MILLISECONDS,
    )) { it.cancel(false) }

fun <T> ((T) -> Unit).async(): (T) -> Unit = { t: T -> async { invoke(t) } }
fun delay(ticks: Long, block: () -> Unit) =
    PluginPortalBase.plugin.executor.schedule(block, ticks * 50, TimeUnit.MILLISECONDS)
fun delay(ticks: Long, async: Boolean, block: () -> Unit) = delay(ticks, block)
