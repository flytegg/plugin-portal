package gg.flyte.common.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import gg.flyte.common.api.plugins.interfaces.PluginApiInterface
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

const val USER_AGENT = "flytegg/plugin-portal/2.0.0 (hello@flyte.gg)"

const val BASE_DOMAIN = "https://api.pluginportal.link/v1/"

val GSON: Gson = GsonBuilder()
    .setPrettyPrinting()
    .setLenient()
    .serializeNulls()
    .create()

val okHttpClient = OkHttpClient()
    .newBuilder()
    .addInterceptor { chain: Interceptor.Chain ->
        chain.proceed(chain.request()).also {
            println("${it.request.url} | ${it.code} | ${it.request.method}")
        }
    }

    .build()

private val pluginApiRetrofit = Retrofit.Builder()
    .client(okHttpClient)
    .baseUrl(BASE_DOMAIN)
    .addConverterFactory(
        GsonConverterFactory.create(GSON)
    )
    .build()

val pluginApiInterface = pluginApiRetrofit.create<PluginApiInterface>()