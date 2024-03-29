package gg.flyte.pluginportal.client

import com.google.gson.GsonBuilder
import de.jensklingenberg.ktorfit.Ktorfit
import gg.flyte.pluginportal.api.type.HashType
import gg.flyte.pluginportal.api.type.MarketplacePlugin
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.cache.storage.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.CacheControl
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.util.*
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Paths

object PPClient {

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .setLenient()
        .create()

    private lateinit var ktorfit: Ktorfit
    private lateinit var ppEndpoints: PPEndpoints
    var client: HttpClient = Builder {}.build()
    private lateinit var userAgent: String

    fun getKtorfit(): Ktorfit {
        if (!PPClient::ktorfit.isInitialized) {
            throw IllegalStateException("Ktorfit has not been initialized! Please use PPClient.Build() first!")
        }

        return ktorfit
    }

    suspend fun searchForPlugins(
        name: String,
        limit: Int = 25,
        offset: Int = 0
    ): PaginatedResult<MarketplacePlugin> {
        return ppEndpoints.searchForPlugins(name, limit, offset)
    }

    suspend fun getPluginById(id: String) = ppEndpoints.getPluginById(id)

    suspend fun recognizePluginByHashes(hashes: HashSet<HashMap<HashType, String>>) = ppEndpoints.recognizePluginByHashes(hashes.encode())

    suspend fun downloadFile(url: String, file: File, callback: suspend (boolean: Boolean, file: File?) -> Unit) {
        try {
            ppEndpoints.downloadFile(url).let {
                file.writeBytes(it)
                callback(true, file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false, null)
        }
    }

    private inline fun <reified T> String.decode() = gson.fromJson(URLDecoder.decode(this, "UTF-8"), T::class.java)
    private inline fun <reified T> T.encode() = URLEncoder.encode(gson.toJson(this), "UTF-8")

    class Builder(init: Builder.() -> Unit) {
        private var userAgent = "flytegg/pp-client"
        var baseUrl = "https://api.pluginportal.link/v1/"

        fun build(): HttpClient {
            client = HttpClient(CIO) {
                install(ContentNegotiation) {
                    gson {
                        setPrettyPrinting()
                    }
                }

                install(UserAgent) {
                    userAgent.apply {
                        PPClient.userAgent = this
                        agent = this
                    }
                }

                install(HttpCache) {
                    val cacheFile = Files.createDirectories(Paths.get("build/cache")).toFile()
                    publicStorage(FileStorage(cacheFile))
                    privateStorage(FileStorage(cacheFile))
                }
            }

            ktorfit = Ktorfit.Builder()
                .baseUrl(baseUrl)
                .httpClient(client)
                .build()

            ppEndpoints = ktorfit.create()

            return client
        }

        init {
            apply(init)
        }
    }

}