package gg.flyte.common.api.interfaces

import gg.flyte.common.type.api.plugin.MarketplacePlugin
import gg.flyte.common.api.dataClasses.endpoints.PaginatedResultMarketplacePlugin
import gg.flyte.common.api.dataClasses.endpoints.RecognizePluginByHashesResult
import gg.flyte.common.type.api.service.PlatformGroup
import gg.flyte.common.type.api.service.PlatformType
import gg.flyte.common.type.api.user.PPPlatform
import gg.flyte.common.type.misc.HashType
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface PluginApiInterface {

    @GET("plugins")
    fun searchForPlugins(
        @Query("name") name: String,
        @Query("platformString") platformString: String,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0
    ): Call<PaginatedResultMarketplacePlugin>

    @GET("plugins/{id}")
    fun getPluginById(@Path("id") id: String): Call<MarketplacePlugin>

    @GET
    @Streaming // Use streaming for large files
    fun downloadFile(@Url url: String): Call<ResponseBody>

    @POST("plugins/{id}/request")
    fun requestPluginById(@Path("id") id: String, @Query("platformString") platformType: PlatformType): Call<Boolean>

    @GET("plugins/recognize")
    fun recognizePluginByHashes(@Query("hashes") hashes: String, @Query("platformGroup") platformGroup: PlatformGroup): Call<RecognizePluginByHashesResult> // SHA256, Plugin

    @GET("versions")
    fun getVersions(@Query("profileString") profileString: String): Call<HashMap<PPPlatform, LinkedHashMap<String, String>>>


}