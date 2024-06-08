package gg.flyte.pluginportal.common

import com.google.gson.Gson
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.Http.BASE_URL
import okhttp3.OkHttpClient

object API {

    private val client = OkHttpClient()

    private fun get(url: String, params: HashMap<String, String>): String {
        val request = okhttp3.Request.Builder()
            .url(
                "$BASE_URL$url?" + params.map { "${it.key}=${it.value}" }.joinToString("&")
            )
            .build()
        val response = client.newCall(request).execute()
        return response.body?.string() ?: ""
    }

    fun getPlugins(prefix: String? = null): List<Plugin> {
        val params = hashMapOf<String, String>()
        if (prefix != null) {
            params["prefix"] = prefix
        }

        return GSON.fromJson(
            get(
                "/plugins",
                params
            ), Array<Plugin>::class.java
        )
            .toList()
    }

}