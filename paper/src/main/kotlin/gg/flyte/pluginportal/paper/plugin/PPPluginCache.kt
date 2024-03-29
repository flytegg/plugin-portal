package gg.flyte.pluginportal.paper.plugin

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.google.common.cache.CacheBuilder
import gg.flyte.pluginportal.api.type.CompactPlugin
import gg.flyte.pluginportal.api.type.MarketplacePlugin
import gg.flyte.pluginportal.paper.PluginPortal
import gg.flyte.twilight.gson.GSON
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

object PPPluginCache {

    /**
     * Key: Search Term
     * Value: HashSet<MarketplacePlugin>
     */
    private val pluginCache = CacheBuilder.newBuilder()
        .expireAfterWrite(60, TimeUnit.MINUTES)
        .build<String, HashSet<MarketplacePlugin>>()

    private val installedPlugins = HashSet<CompactPlugin>()

    private val installedPluginsConfig get(): File = File(PluginPortal.instance.dataFolder, "plugins.json")

    val pluginFolder get(): File = PluginPortal.instance.dataFolder.parentFile
    val updateFolder get(): File = File(pluginFolder, "update").apply { if (!exists()) mkdirs() }

    fun getCachedPlugins() = pluginCache.asMap().values.flatten()

    fun getInstalledPlugins() = installedPlugins
    fun removeInstalledPlugins(vararg plugin: CompactPlugin) = installedPlugins.removeAll(plugin.toSet())

    fun addInstalledPlugins(vararg plugin: CompactPlugin, shouldSave: Boolean = true) = installedPlugins.addAll(plugin)
        .also { if (shouldSave) saveInstalledPlugins() }

    fun addInstalledPlugins(plugin: HashSet<CompactPlugin>) = installedPlugins.addAll(plugin)

    fun getPluginsByName(name: String) = arrayListOf<MarketplacePlugin>().apply {
        pluginCache.asMap().keys.forEach {
            if (name.startsWith(it, true)) {
                addAll(pluginCache.asMap()[it]!!)
            }
        }

        filter { it.displayInfo.name.startsWith(name, true) }
    }

    suspend fun searchForPluginsByName(name: String): List<MarketplacePlugin> {
        mutableListOf<MarketplacePlugin>().apply {
            addAll(getPluginsByName(name))

            if (size >= 25 || pluginCache.asMap().keys.any { name.startsWith(it, true) })
                return this.filter { it.displayInfo.name.startsWith(name, true) }
            else {
                PluginManager.searchForPlugins(name).let { list ->
                    pluginCache.put(name, list)

                    return list.filter {
                        it.displayInfo.name.startsWith(name) || it.displayInfo.name.contains(name)
                    }
                }
            }
        }
    }

    suspend fun loadInstalledPlugins() {
        withContext(PluginPortal.asyncContext) {

            if (installedPluginsConfig.readText().isEmpty()) {
                installedPluginsConfig.writeText("[]")
            }

            GSON.fromJson(
                installedPluginsConfig.readText(),
                Array<CompactPlugin>::class.java
            ).forEach { addInstalledPlugins(it) }

//            val requestHashes = HashSet<HashMap<HashType, String>>().apply {
//                pluginFolder.listFiles()
//                    ?.filter { it.name.endsWith(".jar") }
//                    ?.filter { file -> file.name.startsWith("[PP]") }
//                    ?.forEach { add(it.getHashes()) }
//            }
//
//            if (requestHashes.isNotEmpty()) {
//                PPClient.recognizePluginByHashes(requestHashes).result.forEach {
//                    addInstalledPlugins(it.toCompactPlugin())
//                }
//            }

            saveInstalledPlugins()
        }
    }

    fun saveInstalledPlugins() {
        installedPluginsConfig.writeText(GSON.toJson(getInstalledPlugins()))
    }

    fun MarketplacePlugin.isInstalled() = getInstalledPlugins().any { it.id == id }
}