package gg.flyte.pluginportal.common

import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.Http.BASE_URL
import okhttp3.OkHttpClient
import java.net.URLEncoder

object API {
    private val client = OkHttpClient()

    fun closeClient() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
        client.cache?.close()
    }

    private fun get(url: String, params: HashMap<String, String>): String {
        val request = okhttp3.Request.Builder()
            .url("$BASE_URL$url?" + params.map { "${it.key}=${it.value}" }.joinToString("&"))
            .build()

        val response = client.newCall(request).execute()
        return response.body?.string() ?: ""
    }

    private class AuthorisationException(code: Int)
        : RuntimeException("Server returned code $code: You do not have permission to access this resource")

    private class PluginRequestFailedException(platform: MarketplacePlatform, id: String)
        : RuntimeException("An unexpected error occurred when attempting to retrieve plugin $id from $platform. " +
            "PLEASE REPORT THIS TO THE PLUGIN PORTAL AUTHORS")

    fun getPlugin(platform: MarketplacePlatform, id: String): Plugin? {
        val response = get("/plugins/${platform.toString().lowercase()}/$id", hashMapOf()).ifEmpty {
            return PluginRequestFailedException(platform, id).printStackTrace().let { null }
        }
        val statusCode: Int = response.substringAfter("\"statusCode\":", "200").slice(0..2).toInt()
        when (statusCode) {
            200 -> return GSON.fromJson(response, Plugin::class.java)
            404 -> return null // not found
            401,403 -> { // not authorised
                AuthorisationException(statusCode).printStackTrace()
                return null
            }
            400 -> { // bad request
                IllegalArgumentException("Server returned code 400: The request for $id on $platform PLEASE REPORT THIS TO THE PLUGIN PORTAL AUTHORS")
                    .printStackTrace()
                return null
            }
            else -> {
                PluginRequestFailedException(platform, id).printStackTrace()
                return null
            }
        }
    }

    fun getPlugins(prefix: String? = null, limit: Int? = 50): List<Plugin> {
        val params = hashMapOf<String, String>()

        if (prefix != null) params["prefix"] = URLEncoder.encode(prefix, "UTF-8")
        if (limit != null) params["limit"] = limit.toString()

        return GSON.fromJson(
            get(
                "/plugins",
                params
            ), Array<Plugin>::class.java
        ).toList()
    }

}