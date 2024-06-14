package gg.flyte.pluginportal.plugin.manager

abstract class PluginCache<T>(private val cache: MutableList<T> = mutableListOf()): MutableList<T> by cache