package gg.flyte.common.api

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import gg.flyte.common.api.dataClasses.MarketplacePlugin
import gg.flyte.common.type.api.service.PlatformType
import java.util.concurrent.TimeUnit

object PPPluginCache {

    /**
     * Key: Search Term
     * Value: List<MarketplacePlugin>
     */
    private val pluginCache: Cache<String, List<MarketplacePlugin>> = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build()

    fun getPluginsByName(name: String) = arrayListOf<MarketplacePlugin>().apply {
        pluginCache.asMap().keys.forEach {
            if (name.startsWith(it, true)) {
                addAll(pluginCache.asMap()[it]!!)
            }
        }

        filter { it.displayInfo.name.startsWith(name, true) }
    }


    fun getPluginById(id: String): MarketplacePlugin? {
        pluginCache.asMap().values.forEach {
            it.forEach { plugin ->
                if (plugin.id == id) return plugin
            }
        }

        return null
    }

    fun searchForPluginsByName(name: String, platformType: PlatformType): List<MarketplacePlugin> {

        val cachedMatches = mutableListOf<MarketplacePlugin>().apply {
            addAll(getPluginsByName(name))

            if (size >= 25 || pluginCache.asMap().keys.any { name.startsWith(it, true) } ) return this
            else {
                API.searchForPluginsByName(name, platformType.name).body()?.result?.let { list ->
                    pluginCache.put(name, list)

                    return list.filter {
                        it.displayInfo.name.startsWith(name) || it.displayInfo.name.contains(name)
                    }
                }
            }
        }

        return cachedMatches
    }
}