package gg.flyte.common.api

import gg.flyte.common.api.dataClasses.endpoints.PaginatedResultMarketplacePlugin
import gg.flyte.common.type.service.PlatformType
import gg.flyte.common.util.apiInterface
import retrofit2.Response

object API {

    fun searchForPluginsByName(
        name: String,
        platformString: String,
        limit: Int = 25,
        offset: Int = 0
    ): Response<PaginatedResultMarketplacePlugin> {
        return apiInterface.searchForPlugins(name, platformString, limit, offset).execute()
    }

    fun getPluginById(id: String): Response<PaginatedResultMarketplacePlugin> {
        return apiInterface.getPluginById(id).execute()
    }

    fun requestPluginById(id: String, platformType: PlatformType): Response<Boolean> {
        return apiInterface.requestPluginById(id, platformType).execute()
    }
}

