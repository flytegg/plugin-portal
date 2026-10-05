package gg.flyte.pluginportal.common.adapters.external

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

interface ExternalPluginProvider {
    val id: String
    fun resolve(config: ExternalPluginConfig): ExternalArtifact
}

internal fun interface ExternalJsonClient {
    fun get(url: String): JsonElement
}

internal object HttpExternalJsonClient : ExternalJsonClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun get(url: String): JsonElement {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("User-Agent", "PluginPortal/1.0")
            .build()

        return client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw ExternalPluginException("Provider request failed with HTTP ${response.code}")
            }
            runCatching { JsonParser.parseString(body) }
                .getOrElse { throw ExternalPluginException("Provider returned invalid JSON") }
        }
    }
}

class ExternalPluginException(message: String) : Exception(message)
