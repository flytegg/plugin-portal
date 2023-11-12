package gg.flyte.common.api

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import gg.flyte.common.api.interfaces.InstalledPluginLoader
import gg.flyte.common.type.api.plugin.InstalledPlugin
import gg.flyte.common.type.api.plugin.schemas.MarketplacePlugin
import gg.flyte.common.type.api.plugin.toInstalledPlugin
import gg.flyte.common.type.misc.HashType
import gg.flyte.common.util.getHashes
import java.io.File
import java.util.concurrent.TimeUnit

object PPPluginCache {

    private lateinit var loader: InstalledPluginLoader

    /**
     * Key: Search Term
     * Value: List<MarketplacePlugin>
     */
    private val pluginCache: Cache<String, HashSet<MarketplacePlugin>> = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build()

    private val installedPlugins = HashSet<InstalledPlugin>()

    fun getInstalledPlugins() = installedPlugins
    fun removeInstalledPlugins(vararg plugin: InstalledPlugin) = installedPlugins.removeAll(plugin.toSet())

    fun addInstalledPlugins(vararg plugin: InstalledPlugin) = installedPlugins.addAll(plugin)
    fun addInstalledPlugins(plugin: ArrayList<InstalledPlugin>) = installedPlugins.addAll(plugin)

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

    fun searchForPluginsByName(name: String): List<MarketplacePlugin> {

        val cachedMatches = mutableListOf<MarketplacePlugin>().apply {
            addAll(getPluginsByName(name))

            if (size >= 25 || pluginCache.asMap().keys.any { name.startsWith(it, true) })
                return this.filter { it.displayInfo.name.startsWith(name, true) }
            else {
                API.searchForPluginsByName(name).body()?.let { list ->
                    pluginCache.put(name, list)

                    return list.filter {
                        it.displayInfo.name.startsWith(name) || it.displayInfo.name.contains(name)
                    }
                }
            }
        }

        return cachedMatches
    }

    fun loadInstalledPlugins(pluginsFolder: File, loader: InstalledPluginLoader) {
        this.loader = loader
        val requestHashes = HashSet<HashMap<HashType, String>>()

        for (file in pluginsFolder.listFiles()!!) {
            if (file.isDirectory || !file.name.endsWith(".jar")) continue

            if (installedPlugins.any { it.hashes!!.any { hash -> file.getHashes().containsValue(hash.value) } }) continue


            println("Found Jar: ${file.name}")

            requestHashes.add(file.getHashes())

        }

        API.recognizePluginByHashes(requestHashes).body()?.forEach {
            println("Recognized: ${it.displayInfo.name}")
            installedPlugins.add(it.toInstalledPlugin())
        }
    }

    fun saveInstalledPlugins() {
        loader.saveInstalledPlugins()
    }
}