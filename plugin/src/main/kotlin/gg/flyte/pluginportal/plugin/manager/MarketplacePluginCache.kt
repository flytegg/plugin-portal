package gg.flyte.pluginportal.plugin.manager

import gg.flyte.pluginportal.common.types.Plugin

object MarketplacePluginCache : PluginCache<Plugin>() {



    fun getPlugins(): List<Plugin> {
        return get()
    }


}