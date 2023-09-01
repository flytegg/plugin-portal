package gg.flyte.common.api

import gg.flyte.common.api.dataClasses.MarketplacePlugin
import gg.flyte.common.api.dataClasses.endpoints.PaginatedResultMarketplacePlugin
import gg.flyte.common.type.service.PlatformGroup
import gg.flyte.common.type.service.PlatformType
import gg.flyte.common.util.pluginApiInterface
import retrofit2.Response

object API {

    fun searchForPluginsByName(
        name: String,
        platformString: String,
        limit: Int = 25,
        offset: Int = 0
    ): Response<PaginatedResultMarketplacePlugin> {
        return pluginApiInterface.searchForPlugins(name, platformString, limit, offset).execute()
    }

    fun getPluginById(id: String): Response<MarketplacePlugin> {
        return pluginApiInterface.getPluginById(id).execute()
    }

    fun requestPluginById(id: String, platformType: PlatformType): Response<Boolean> {
        return pluginApiInterface.requestPluginById(id, platformType).execute()
    }

    fun recognizePluginByHashes(hashes: String, platformGroup: PlatformGroup): Response<HashMap<String, MarketplacePlugin>> {
        return pluginApiInterface.recognizePluginByHashes(hashes, platformGroup).execute()
    }

    fun getVersions(): Response<HashMap<String, String>> {
        return pluginApiInterface.getVersions().execute()
    }
}

