package gg.flyte.pluginportal.common.managers

abstract class PluginCache<T>(private val cache: MutableSet<T> = java.util.concurrent.CopyOnWriteArraySet()): MutableSet<T> by cache