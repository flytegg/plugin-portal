package gg.flyte.pluginportal.plugin.http

import com.google.common.cache.CacheBuilder
import gg.flyte.pluginportal.common.API
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

        val response = API.getPlugins(query).also {
            searchCache.put(query, it)
        }

        return response
    }
}