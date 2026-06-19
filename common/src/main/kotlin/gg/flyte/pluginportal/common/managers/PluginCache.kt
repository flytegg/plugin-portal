package gg.flyte.pluginportal.common.managers

abstract class PluginCache<T>(private val cache: MutableSet<T> = mutableSetOf()): MutableSet<T> by cache