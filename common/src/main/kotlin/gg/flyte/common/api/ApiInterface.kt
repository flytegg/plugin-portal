package gg.flyte.common.api

import gg.flyte.common.api.dataClasses.endpoints.PaginatedResultMarketplacePlugin
import gg.flyte.common.type.service.PlatformType
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @GET("plugins")
    fun searchForPlugins(
        @Query("name") name: String,
        @Query("platformString") platformString: String,
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0
    ): Call<PaginatedResultMarketplacePlugin>

    @GET("plugins/{id}")
    fun getPluginById(@Path("id") id: String): Call<PaginatedResultMarketplacePlugin>

    @GET
    @Streaming // Use streaming for large files
    fun downloadFile(@Url url: String): Call<ResponseBody>

    @POST("plugins/{id}/request")
    fun requestPluginById(@Path("id") id: String, @Query("platformString") platformType: PlatformType): Call<Boolean>


}