package gg.flyte.common.api

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import gg.flyte.common.type.api.plugin.MarketplacePlugin
import gg.flyte.common.type.api.plugin.VersionInfo
import gg.flyte.common.api.interfaces.InstalledPluginLoader
import gg.flyte.common.type.api.plugin.InstalledPlugin
import gg.flyte.common.type.api.service.PlatformGroup
import gg.flyte.common.type.api.service.PlatformType
import gg.flyte.common.util.get256Hash
import gg.flyte.common.util.toJson
import java.io.File
import java.util.concurrent.TimeUnit

object PPPluginCache {

    private lateinit var loader: InstalledPluginLoader

    /**
     * Key: Search Term
     * Value: List<MarketplacePlugin>
     */
    private val pluginCache: Cache<String, List<MarketplacePlugin>> = Caffeine.newBuilder()
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

    fun searchForPluginsByName(name: String, platformType: PlatformType): List<MarketplacePlugin> {

        val cachedMatches = mutableListOf<MarketplacePlugin>().apply {
            addAll(getPluginsByName(name))

            if (size >= 25 || pluginCache.asMap().keys.any { name.startsWith(it, true) })
                return this.filter { it.displayInfo.name.startsWith(name, true) }
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

    fun loadInstalledPlugins(pluginsFolder: File, loader: InstalledPluginLoader) {
        this.loader = loader
        val requestHashes = arrayListOf<String>()

        for (file in pluginsFolder.listFiles()!!) {
            if (file.isDirectory || !file.name.endsWith(".jar")) continue
            if (installedPlugins.any { it.sha256Hash == file.get256Hash() }) continue

            println("Found Jar: ${file.name}")
            requestHashes.add(file.get256Hash())
        }

        API.recognizePluginByHashes(requestHashes, PlatformGroup.CRAFT_BUKKIT).body()?.let { map ->
            map.forEach { (hash, plugin) ->
                var pluginVersionData: VersionInfo? = null
                var pluginVersion = ""
                var pluginPlatform: PlatformType? = null

                plugin.versions.forEach { entry ->
                    entry.value.forEach { versions ->
                        if (versions.value.shaHash == hash) {
                            pluginVersionData = versions.value
                            pluginVersion = versions.key
                            pluginPlatform = entry.key
                        }
                    }
                }

                println("Auto Recognized Plugin: ${plugin.displayInfo.name} | Version: $pluginVersion | Platform: $pluginPlatform")

                val installedPlugin = InstalledPlugin(
                    plugin.id,
                    plugin.displayInfo.name,
                    pluginVersion,
                    pluginPlatform!!,
                    plugin.primaryServiceType,
                    hash,
                    pluginVersionData!!.downloadUrl,
                )

                println(installedPlugin.toJson())

                loader.addInstalledPlugin(installedPlugin)
            }
        }

        saveInstalledPlugins()
    }

    fun saveInstalledPlugins() {
        loader.saveInstalledPlugins()
    }
}