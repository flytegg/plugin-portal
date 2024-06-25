package gg.flyte.pluginportal.common

import com.google.common.cache.CacheBuilder
import gg.flyte.pluginportal.common.types.Plugin
import java.util.concurrent.TimeUnit

object SearchPlugins {
    private val searchCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build<String, List<Plugin>>()

    fun search(query: String): List<Plugin> {
        if (searchCache.asMap().containsKey(query)) {
            return searchCache.getIfPresent(query)!!
        }

        val response = API.getPlugins(query, 100).also {
            searchCache.put(query, it)
        }

        return response
    }

    fun getCachedSearch(query: String): List<Plugin>? {
        searchCache.asMap().forEach { (key, value) ->
            if (query.contains(key, ignoreCase = true)) {
                return value
            }
        }

        return null
    }
}
