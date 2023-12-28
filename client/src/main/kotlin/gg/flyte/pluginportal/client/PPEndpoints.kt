package gg.flyte.pluginportal.client

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import de.jensklingenberg.ktorfit.http.Url
import gg.flyte.pluginportal.api.type.MarketplacePlugin
import java.io.File

interface PPEndpoints {

    @GET("plugins")
    suspend fun searchForPlugins(
        @Query("name") name: String,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0
    ): PaginatedResult<MarketplacePlugin>

    @GET("plugins/{id}")
    suspend fun getPluginById(@Path("id") id: String): MarketplacePlugin?

    @GET("recognize")
    suspend fun recognizePluginByHashes(@Query("hashes") hashes: String): PaginatedResult<MarketplacePlugin>

    @GET
    suspend fun downloadFile(@Url url: String): ByteArray

}