package gg.flyte.pluginportal.common

import com.google.common.cache.CacheBuilder
import gg.flyte.pluginportal.common.types.Plugin
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

object SearchPlugins {
    private val searchCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build<String, List<Plugin>>()

    fun search(query: String): List<Plugin> = search(query) { API.getPlugins(it) }

    internal fun search(query: String, fetch: (String) -> Array<Plugin>?): List<Plugin> {
        return try {
            searchCache.get(query) {
                fetch(query)?.toList() ?: throw PluginSearchUnavailableException()
            }
        } catch (_: ExecutionException) {
            emptyList()
        }
    }

    fun getCachedSearch(query: String): List<Plugin>? {
        return searchCache.asMap().entries
            .firstOrNull { (key, _) -> query.contains(key, ignoreCase = true) }
            ?.value
    }
}

private class PluginSearchUnavailableException : Exception()
