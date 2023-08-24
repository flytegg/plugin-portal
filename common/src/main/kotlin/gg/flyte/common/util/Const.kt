package gg.flyte.common.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import gg.flyte.common.api.ApiInterface
import gg.flyte.common.api.RequestInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

const val USER_AGENT = "flytegg/pp-lib/2.0.0 (hello@flyte.gg)"

//const val BASE_DOMAIN = "https://api.portalbox.link/v1/"
const val BASE_DOMAIN = "http://localhost:5005/v1/"

val GSON: Gson = GsonBuilder().setPrettyPrinting().create()

val okHttpClient = OkHttpClient()
    .newBuilder()
    .addInterceptor(RequestInterceptor())
    .build()

val retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .baseUrl(BASE_DOMAIN)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiInterface = retrofit.create<ApiInterface>()