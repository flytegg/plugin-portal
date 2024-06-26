package gg.flyte.pluginportal.plugin.manager

abstract class PluginCache<T>(private val cache: MutableSet<T> = mutableSetOf()): MutableSet<T> by cache