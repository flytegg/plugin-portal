package gg.flyte.pluginportal.plugin.manager

abstract class PluginCache <T> {

    private val cache = mutableListOf<T>()

    fun get() = cache
    fun add(plugin: T) = cache.add(plugin)
    fun remove(plugin: T) = cache.remove(plugin)


    fun size() = cache.size
    fun clear() = cache.clear()
    fun isEmpty() = cache.isEmpty()
    fun isNotEmpty() = cache.isNotEmpty()



}