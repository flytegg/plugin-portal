package gg.flyte.common.api.plugins.interfaces

import gg.flyte.common.api.plugins.schemas.MarketplacePlugin
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface PluginApiInterface {

    @GET("plugins")
    fun searchForPlugins(
        @Query("name") name: String,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0
    ): Call<HashSet<MarketplacePlugin>>

    @GET("plugins/{id}")
    fun getPluginById(@Path("id") id: String): Call<MarketplacePlugin>

    @GET("recognize")
    fun recognizePluginByHashes(@Query("hashes") hashes: String): Call<HashSet<MarketplacePlugin>> // SHA256, Plugin

    @GET
    @Streaming // Use streaming for large files
    fun downloadFile(@Url url: String): Call<ResponseBody>


}