package gg.flyte.pluginPortal.type.manager

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import gg.flyte.common.api.API
import gg.flyte.common.api.plugins.schemas.InstalledPlugin
import gg.flyte.common.api.plugins.schemas.MarketplacePlugin
import gg.flyte.common.api.plugins.schemas.toInstalledPlugin
import gg.flyte.common.api.plugins.schemas.HashType
import gg.flyte.common.util.GSON
import gg.flyte.common.util.getHashes
import gg.flyte.pluginPortal.PluginPortal
import gg.flyte.twilight.scheduler.async
import java.io.File
import java.util.concurrent.TimeUnit

object PPPluginCache {

    /**
     * Key: Search Term
     * Value: HashSet<MarketplacePlugin>
     */
    private val pluginCache: Cache<String, HashSet<MarketplacePlugin>> = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build()

    private val installedPlugins = HashSet<InstalledPlugin>()

    private val installedPluginsConfig get(): File = File(PluginPortal.instance.dataFolder, "plugins.json")
    val pluginFolder get(): File = PluginPortal.instance.dataFolder.parentFile
    val updateFolder get(): File = File(pluginFolder, "update").apply { if (!exists()) mkdirs() }

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

    fun loadInstalledPlugins() {
        async {
            if (installedPluginsConfig.readText().isEmpty()) {
                installedPluginsConfig.writeText("[]")
            }

            GSON.fromJson(
                installedPluginsConfig.readText(),
                Array<InstalledPlugin>::class.java
            ).forEach { addInstalledPlugins(it) }

            val requestHashes = HashSet<HashMap<HashType, String>>().apply {
                pluginFolder.listFiles()
                    ?.filter { it.name.endsWith(".jar") }
                    ?.filter { file ->
                        !installedPlugins.any {
                            it.hashes?.firstNotNullOfOrNull { hash ->
                                file.getHashes().containsValue(hash.value)
                            } ?: false
                        }
                    }
                    ?.forEach { add(it.getHashes()) }
            }

            API.recognizePluginByHashes(requestHashes).body()?.forEach {
                addInstalledPlugins(it.toInstalledPlugin())
            }

            saveInstalledPlugins()
        }
    }

    fun saveInstalledPlugins() {
        installedPluginsConfig.writeText(GSON.toJson(getInstalledPlugins()))
    }

    fun MarketplacePlugin.isInstalled() = getInstalledPlugins().any { it.id == id }
}