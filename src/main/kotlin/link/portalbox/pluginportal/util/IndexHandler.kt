package link.portalbox.pluginportal.util

import com.google.common.collect.BiMap
import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.type.Config
import link.portalbox.pplib.manager.MarketplacePluginManager.loadIndex
import link.portalbox.pplib.manager.MarketplacePluginManager.marketplaceCache
import org.bukkit.Bukkit
import java.util.logging.Logger
import kotlin.math.roundToLong

var lastUpdate = System.currentTimeMillis()

fun getMarketplaceCache(): BiMap<String, String> {
    if (marketplaceCache.values.size < 10) {
        loadIndex()
    }

    lastUpdate = System.currentTimeMillis()
    return marketplaceCache
}

fun startCacheTask(pluginPortal: PluginPortal) {
    Bukkit.getScheduler().runTaskTimerAsynchronously(pluginPortal, Runnable {
        if (System.currentTimeMillis() - lastUpdate > Config.cacheTime * 60000) {
            getMarketplaceCache().clear()
            lastUpdate = System.currentTimeMillis()
        }
    }, 0L, (Config.cacheTime * 1200).toDouble().roundToLong()) // 1200 ticks in a minute


}