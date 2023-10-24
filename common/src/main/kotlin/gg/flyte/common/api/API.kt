package gg.flyte.common.api

import gg.flyte.common.type.api.plugin.MarketplacePlugin
import gg.flyte.common.api.dataClasses.endpoints.PaginatedResultMarketplacePlugin
import gg.flyte.common.api.dataClasses.endpoints.RecognizePluginByHashesResult
import gg.flyte.common.type.misc.HashType
import gg.flyte.common.util.encodeURL
import gg.flyte.common.util.pluginApiInterface
import retrofit2.Response

object API {

    fun searchForPluginsByName(
        name: String,
        limit: Int = 25,
        offset: Int = 0
    ): Response<PaginatedResultMarketplacePlugin> {
        return pluginApiInterface.searchForPlugins(name, limit, offset).execute()
    }

    fun getPluginById(id: String): Response<MarketplacePlugin> {
        return pluginApiInterface.getPluginById(id).execute()
    }

    fun requestPluginById(id: String): Response<Boolean> {
        return pluginApiInterface.requestPluginById(id).execute()
    }

    fun recognizePluginByHashes(hashes: HashMap<HashType, String>): Response<RecognizePluginByHashesResult> {
        return pluginApiInterface.recognizePluginByHashes(hashes.encodeURL()).execute()
    }

    // TODO: Create a /v1/versions/SPIGOT endpoint
//    fun getVersions(profile): Response<HashMap<PPPlatform, LinkedHashMap<String, String>>> {
//        return pluginApiInterface.getVersions(profile.toJson()).execute()
//    }
}

