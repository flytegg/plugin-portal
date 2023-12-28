package gg.flyte.pluginportal.api

import gg.flyte.pluginportal.api.type.MarketplacePlugin
import java.io.File

abstract class PluginPortalAPI {

    abstract fun getPlugin(id: String): MarketplacePlugin?

    abstract fun searchForPlugins(query: String): HashSet<MarketplacePlugin>

    abstract fun installPlugin(plugin: MarketplacePlugin, after: (Boolean) -> Unit = {})
    fun installPlugin(id: String, after: (Boolean) -> Unit = {}) = installPlugin(getPlugin(id)!!) { after(it) }

    fun MarketplacePlugin.install(after: (Boolean) -> Unit = {}) = installPlugin(this) { after(it) }
}