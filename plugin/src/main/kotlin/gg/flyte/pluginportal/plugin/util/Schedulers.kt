package gg.flyte.pluginportal.plugin.util

import gg.flyte.pluginportal.plugin.PluginPortal
import org.bukkit.Bukkit

fun async(block: () -> Unit) =
    Bukkit.getScheduler().runTaskAsynchronously(PluginPortal.instance, block)

fun delay(ticks: Long, block: () -> Unit) =
    Bukkit.getScheduler().runTaskLater(PluginPortal.instance, block, ticks)