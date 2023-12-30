package gg.flyte.pluginportal.api

import gg.flyte.pluginportal.api.type.MarketplacePlugin
import java.io.File

abstract class PluginPortalAPI {

    abstract suspend fun getPlugin(id: String): MarketplacePlugin?

    abstract suspend fun installPlugin(plugin: MarketplacePlugin, after: (Boolean) -> Unit = {})
    suspend fun installPlugin(id: String, after: (Boolean) -> Unit = {}) = installPlugin(getPlugin(id)!!) { after(it) }

    suspend fun MarketplacePlugin.install(after: (Boolean) -> Unit = {}) = installPlugin(this) { after(it) }
}