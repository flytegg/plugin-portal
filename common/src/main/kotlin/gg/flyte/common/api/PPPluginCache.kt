package gg.flyte.common.api

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import gg.flyte.common.api.dataClasses.MarketplacePlugin
import gg.flyte.common.type.api.service.PlatformType
import java.security.Key
import java.util.concurrent.TimeUnit
import javax.lang.model.element.Name

object PPPluginCache {

    /**
     * Key: Plugin Name
     * Value: MarketplacePlugin
     */
    val pluginCache: Cache<String, MarketplacePlugin> = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build()

    fun getPluginByName(name: String): MarketplacePlugin? {
        return pluginCache.getIfPresent(name)
    }

    fun getPluginById(id: String): MarketplacePlugin? {
        return pluginCache.asMap().values.find { it.id == id }
    }

    fun searchForPluginsByName(name: String, platformType: PlatformType): List<MarketplacePlugin> {
        API.searchForPluginsByName(name, platformType.name).body()?.result?.let { list ->
            list.forEach {
                pluginCache.put(it.displayInfo.name, it)
            }
        }

        return pluginCache.asMap()
            .filter { it.key.startsWith(name) || it.key.contains(name) }
            .map { it.value }
    }
}