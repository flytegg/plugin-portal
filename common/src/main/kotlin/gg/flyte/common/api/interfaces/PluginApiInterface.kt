package gg.flyte.common.api.interfaces

import gg.flyte.common.type.api.plugin.MarketplacePlugin
import gg.flyte.common.api.dataClasses.endpoints.PaginatedResultMarketplacePlugin
import gg.flyte.common.api.dataClasses.endpoints.RecognizePluginByHashesResult
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface PluginApiInterface {

    @GET("plugins")
    fun searchForPlugins(
        @Query("name") name: String,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0
    ): Call<PaginatedResultMarketplacePlugin>

    @GET("plugins/{id}")
    fun getPluginById(@Path("id") id: String): Call<MarketplacePlugin>

    @GET("recognize")
    fun recognizePluginByHashes(@Query("hashes") hashes: String): Call<HashSet<MarketplacePlugin>> // SHA256, Plugin

    @GET
    @Streaming // Use streaming for large files
    fun downloadFile(@Url url: String): Call<ResponseBody>


}