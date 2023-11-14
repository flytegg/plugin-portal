package gg.flyte.common.api

import com.google.gson.Gson

import gg.flyte.common.api.plugins.schemas.MarketplacePlugin
import gg.flyte.common.api.plugins.schemas.HashType
import gg.flyte.common.util.pluginApiInterface
import gg.flyte.common.util.toJson
import retrofit2.Response
import java.net.URLDecoder
import java.net.URLEncoder

object API {

    fun searchForPluginsByName(
        name: String,
        limit: Int = 25,
        offset: Int = 0
    ): Response<HashSet<MarketplacePlugin>> {
        return pluginApiInterface.searchForPlugins(name, limit, offset).execute()
    }

    fun getPluginById(id: String): Response<MarketplacePlugin> {
        return pluginApiInterface.getPluginById(id).execute()
    }

    fun recognizePluginByHashes(hashes: HashSet<HashMap<HashType, String>>): Response<HashSet<MarketplacePlugin>> {
        return pluginApiInterface.recognizePluginByHashes(hashes.encode()).execute()
    }
}

private inline fun <reified T> String.decode() = Gson().fromJson(URLDecoder.decode(this, Charsets.UTF_8), T::class.java)
private inline fun <reified T> T.encode() = URLEncoder.encode(this?.toJson(), Charsets.UTF_8)
