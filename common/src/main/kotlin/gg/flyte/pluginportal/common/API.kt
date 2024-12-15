package gg.flyte.pluginportal.common

import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.Http.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

object API {
    private val client = OkHttpClient()

    fun closeClient() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
        client.cache?.close()
    }

    private fun get(url: String, params: Map<String, String>): Pair<String, Int> {
        val fullUrl = buildString {
            append(BASE_URL)
            append(url)
            if (params.isNotEmpty()) {
                append("?")
                append(params.map { (key, value) ->
                    "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
                }.joinToString("&"))
            }
        }

        val request = Request.Builder().url(fullUrl).build()
        return client.newCall(request).execute().use { response ->
            (response.body?.string() ?: "") to response.code
        }
    }


    private class AuthorisationException(code: Int)
        : RuntimeException("Server returned code $code: You do not have permission to access this resource")

    private class PluginRequestFailedException(platform: MarketplacePlatform, id: String)
        : RuntimeException("An unexpected error occurred when attempting to retrieve plugin $id from $platform. " +
            "PLEASE REPORT THIS TO THE PLUGIN PORTAL AUTHORS")

    fun getPlugin(platform: MarketplacePlatform, id: String): Plugin? {
        val (response, code) = get("/plugins/${platform.toString().lowercase()}/$id", emptyMap())
        return when (code) {
            200 -> GSON.fromJson(response, Plugin::class.java)
            404 -> null // not found
            401, 403 -> null.also { AuthorisationException(code).printStackTrace() } // not authorised
            400 -> null.also {
                IllegalArgumentException("Server returned code 400: The request for $id on $platform PLEASE REPORT THIS TO THE PLUGIN PORTAL AUTHORS").printStackTrace()
            } // bad request
            else -> null.also { PluginRequestFailedException(platform, id).printStackTrace() }
        }.takeIf { response.isNotEmpty() }
    }

    fun getPlugins(prefix: String? = null, limit: Int? = 50): List<Plugin> {
        val params = buildMap {
            prefix?.let { put("prefix", it) }
            limit?.let { put("limit", it.toString()) }
        }

        val (response, _) = get("/plugins", params)
        return GSON.fromJson(response, Array<Plugin>::class.java).toList()
    }

    fun getAllPluginsByPlatformIds(platformIds: List<PlatformId>): Array<Plugin>? {
        val (response, code) = get("/plugins", mapOf(
            "platformIds" to GSON.toJson(platformIds)
        ))

        return when (code) {
            200 -> GSON.fromJson<Array<Plugin>>(response, Array<Plugin>::class.java)
            404 -> null // not found
            401, 403 -> null.also { AuthorisationException(code).printStackTrace() } // not authorised
            else -> null
        }.takeIf { response.isNotEmpty() }
    }
}

data class PlatformId(
    val platform: MarketplacePlatform,
    val id: String
)