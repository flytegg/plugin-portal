package gg.flyte.pluginportal.common

import com.google.common.cache.CacheBuilder
import gg.flyte.pluginportal.common.types.Plugin
import java.util.concurrent.TimeUnit

object SearchPlugins {
    private val searchCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build<String, List<Plugin>>()

    fun search(query: String): List<Plugin> {
        return searchCache.get(query) { API.getPlugins(query) }
    }

    fun getCachedSearch(query: String): List<Plugin>? {
        return searchCache.asMap().entries
            .firstOrNull { (key, _) -> query.contains(key, ignoreCase = true) }
            ?.value
    }
}