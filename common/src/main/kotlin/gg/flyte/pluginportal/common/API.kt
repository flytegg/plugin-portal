package gg.flyte.pluginportal.common

import com.google.gson.reflect.TypeToken
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.types.Pagination
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.types.Version
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.types.enums.ServerType
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.download
import gg.flyte.pluginportal.common.util.HttpInfo
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URL
import java.net.URI

data class AuthCreds(
    val mclKey: String, val serverIp: String
)

// ORPC Response Types (minimal, focused on what the plugin needs)
private data class ORPCVersionsResponse(val versions: List<ORPCVersionInfo>)
private data class ORPCVersionInfo(val version: String, val fullVersion: String, val channel: String, val stable: Boolean, val filename: String)
private data class ORPCPlatformVersionsResponse(val versions: Array<Version>, val pagination: Pagination)
private data class ORPCUpdateResponse(val updateAvailable: Boolean, val current: ORPCVersionChannel, val latest: ORPCLatestVersion?)
private data class ORPCVersionChannel(val version: String, val channel: String)  
private data class ORPCLatestVersion(val version: String, val channel: String, val downloadUrl: String, val changelog: String?)
private data class ORPCPluginsResponse(val plugins: Array<Plugin>, val pagination: Pagination)
private data class ORPCPlugin(val _id: String, val name: String, val totalDownloads: Long?)
data class EntryIdMap(val modrinth: List<String>, val hangar: List<String>, val spigotmc: List<String>, val polymart: List<String>)

data class Hash(val sha256: String, val sha512: String)

private const val MAX_PLATFORM_VERSION_PAGE_SIZE = 500
private const val MAX_PLATFORM_VERSION_PAGES = 20

internal fun nextPlatformVersionsOffset(pagination: Pagination, currentOffset: Int, receivedCount: Int): Int? {
    if (!pagination.hasMore || receivedCount <= 0) return null

    val nextOffset = pagination.offset + pagination.limit
    return nextOffset.takeIf { it > currentOffset }
}

object API {
    private val client = OkHttpClient().newBuilder().build()
    private var authKey: String? = null
    private data class ApiResponse(val body: String, val code: Int)

    fun enableAuthenticatedClient(mclKey: String? = null) {
        authKey = mclKey?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun closeClient() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
        client.cache?.close()
    }

    private fun orpcCall(
        endpoint: String,
        params: Map<String, String> = emptyMap(),
        body: String? = null,
        overrideAuth: String? = null
    ): ApiResponse {
        val baseUrl = URI(HttpInfo.getApiBaseUrl()).toURL()
        val url = HttpUrl.Builder()
            .scheme(baseUrl.protocol)
            .host(baseUrl.host)
            .apply {
                if (baseUrl.port != -1) port(baseUrl.port)
            }
            .addPathSegments(endpoint.let { if (it.startsWith("/")) it.substring(1) else it })
            .apply {
                params.forEach { (key, value) ->
                    addQueryParameter(key, value)
                }
            }
            .build()

        return try {
            val requestBuilder = Request.Builder().url(url).apply {
                body?.let { post(it.toRequestBody("application/json".toMediaType())) }
                (overrideAuth ?: authKey)?.let {
                    header("Authorization", "Bearer $it")
                    if (overrideAuth == null) header("x-api-key", it)
                }
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                ApiResponse(response.body?.string() ?: "", response.code)
            }
        } catch (e: Exception) {
            PluginPortalBase.plugin.logger.warning("Plugin Portal API may be down. Request failed for $url: ${e.message ?: e::class.simpleName}")
            ApiResponse("", 500)
        }
    }

    private fun ApiResponse.isSuccessful() = code in 200..299 && body.isNotEmpty()

    private fun logRequestFailure(action: String, response: ApiResponse, authSensitive: Boolean = false) {
        val reason = when (response.code) {
            401, 403 -> if (authSensitive) "authentication was rejected" else "request was not authorized"
            404 -> "endpoint or resource was not found"
            429 -> "API rate limit was reached"
            in 500..599 -> "Plugin Portal API may be down"
            else -> "server returned HTTP ${response.code}"
        }

        PluginPortalBase.plugin.logger.warning("$action skipped: $reason.")
    }

    private fun logParseFailure(action: String, error: Throwable) {
        PluginPortalBase.plugin.logger.warning("$action failed: API returned an unexpected response (${error.message ?: error::class.simpleName}).")
    }

    fun getPlugins(prefix: String? = null, limit: Int? = 50, platform: MarketplacePlatform? = null): Array<Plugin>? {
        val params = buildMap {
            prefix?.let { put("prefix", it) }
            limit?.let { put("limit", it.toString()) }
            platform?.let { put("platform", platform.toString().lowercase()) }
        }

        val response = orpcCall("/plugins", params)

        return runCatching {
            GSON.fromJson(response.body, ORPCPluginsResponse::class.java).plugins // TODO: Currently ignoring pagination data
        }.onFailure {
            logParseFailure("Plugin search", it)
        }.getOrNull()
    }

    fun getPluginById(id: String): Plugin? {
        val response = orpcCall("/plugins/$id")

        if (!response.isSuccessful()) {
            logRequestFailure("Plugin lookup", response)
            return null
        }

        return runCatching {
            GSON.fromJson(response.body, Plugin::class.java)
        }.onFailure {
            logParseFailure("Plugin lookup", it)
        }.getOrNull()
    }

    fun checkForPPUpdate(currentVersion: String): UpdateCheckResponse? {
        val response = orpcCall("/versions/check-update", mapOf(
            "current" to currentVersion,
            "type" to "free",
        ))

        if (!response.isSuccessful()) {
            logRequestFailure("Update check", response)
            return null
        }

        return runCatching {
            GSON.fromJson(response.body, UpdateCheckResponse::class.java)
        }.onFailure { logParseFailure("Update check", it) }
            .getOrNull()
    }

    fun downloadPluginPortalUpdate(newVersion: String, channel: String = "stable"): Boolean {
        val url = URL("${HttpInfo.getApiBaseUrl()}/versions/$newVersion/$channel/download?type=free")

        val to = File(Constants.UPDATE_DIRECTORY, PluginPortalBase.info.getJarName(newVersion))
        val creds = authKey?.let { AuthCreds(mclKey = it, serverIp = "") }
        val result = download(url, to, null, creds)
        return result != null
    }

    fun recordPluginPortalStartup(
        serverId: String,
        licenseKeyHash: String?,
        licenseKeyMasked: String?,
        version: String,
        serverVersion: String?,
        minecraftVersion: String?,
        managedPluginCount: Int,
        pluginPortalUpdateSuccessCount: Int,
        pluginPortalUpdateFailureCount: Int,
        managedPluginUpdateFailureCount: Int
    ) {
        val body = GSON.toJson(mapOf(
            "serverId" to serverId,
            "licenseKeyHash" to licenseKeyHash,
            "licenseKeyMasked" to licenseKeyMasked,
            "version" to version,
            "pluginType" to "free",
            "serverVersion" to serverVersion,
            "minecraftVersion" to minecraftVersion,
            "managedPluginCount" to managedPluginCount,
            "pluginPortalUpdateSuccessCount" to pluginPortalUpdateSuccessCount,
            "pluginPortalUpdateFailureCount" to pluginPortalUpdateFailureCount,
            "managedPluginUpdateFailureCount" to managedPluginUpdateFailureCount,
        ))
        val response = orpcCall("/versions/telemetry/startup", body = body)
        if (!response.isSuccessful()) logRequestFailure("Release startup telemetry", response)
    }

    fun recordPluginPortalUpdateQueued(
        serverId: String,
        licenseKeyHash: String?,
        licenseKeyMasked: String?,
        currentVersion: String,
        targetVersion: String,
        serverVersion: String?,
        minecraftVersion: String?,
        managedPluginCount: Int,
        pluginPortalUpdateSuccessCount: Int,
        pluginPortalUpdateFailureCount: Int,
        managedPluginUpdateFailureCount: Int
    ) {
        val body = GSON.toJson(mapOf(
            "serverId" to serverId,
            "licenseKeyHash" to licenseKeyHash,
            "licenseKeyMasked" to licenseKeyMasked,
            "currentVersion" to currentVersion,
            "targetVersion" to targetVersion,
            "pluginType" to "free",
            "serverVersion" to serverVersion,
            "minecraftVersion" to minecraftVersion,
            "managedPluginCount" to managedPluginCount,
            "pluginPortalUpdateSuccessCount" to pluginPortalUpdateSuccessCount,
            "pluginPortalUpdateFailureCount" to pluginPortalUpdateFailureCount,
            "managedPluginUpdateFailureCount" to managedPluginUpdateFailureCount,
        ))
        val response = orpcCall("/versions/telemetry/update-queued", body = body)
        if (!response.isSuccessful()) logRequestFailure("Release update telemetry", response)
    }
    /*

//                UpdateCheckResponse(
//                    updateAvailable = updateResponse.updateAvailable,
//                    current = VersionChannel(
//                        version = updateResponse.current.version,
//                        channel = updateResponse.current.channel
//                    ),
//                    latest = updateResponse.latest?.let { latest ->
//                        LatestVersion(
//                            version = latest.version,
//                            channel = latest.channel,
//                            downloadUrl = latest.downloadUrl,
//                            changelog = latest.changelog
//                        )
//                    }
//                )
//    }

//    fun checkForFreeUpdate(currentVersion: String): UpdateCheckResponse? {
//        val (response, code) = orpcCall("/versions/check-update", mapOf(
//            "current" to currentVersion,
//            "type" to "free",
//            "includePrerelease" to "true"
//        ))
//
//        return if (code == 200 && response.isNotEmpty()) {
//            try {
//                val updateResponse = GSON.fromJson(response, ORPCUpdateResponse::class.java)
//                UpdateCheckResponse(
//                    updateAvailable = updateResponse.updateAvailable,
//                    current = VersionChannel(
//                        version = updateResponse.current.version,
//                        channel = updateResponse.current.channel
//                    ),
//                    latest = updateResponse.latest?.let { latest ->
//                        LatestVersion(
//                            version = latest.version,
//                            channel = latest.channel,
//                            downloadUrl = latest.downloadUrl,
//                            changelog = latest.changelog
//                        )
//                    }
//                )
//            } catch (e: Exception) {
//                null
//            }
//        } else {
//            null
//        }
//    }
*/
    // VERSION CHECK - ENDS

    data class PluginRecognitionResponse(
        val version: String?,
        val matchedSha256: String?,
        val matchedServerTypes: Array<ServerType>?,
        val plugin: Plugin?
    )
    private val recognisePluginMapToken = object: TypeToken<Map<String, PluginRecognitionResponse?>>() {}

    fun recognizePlugins(hashes: List<Hash>): Map<String, PluginRecognitionResponse?>? {
        val response = orpcCall("/recognize", body = GSON.toJson(hashes.toTypedArray()))

        if (!response.isSuccessful()) {
            logRequestFailure("Plugin recognition", response)
            return null
        }

        return runCatching {
            GSON.fromJson(response.body, recognisePluginMapToken)
        }.onFailure {
            logParseFailure("Plugin recognition", it)
        }.getOrNull()
    }

    fun fetchLocalPluginRemotes(): Map<String, Plugin?>? = getAllPluginsByEntryId(LocalPluginCache.getEntryIdMap())

    fun getPluginByPlatformId(platformId: PlatformId): Plugin? {
        return getAllPluginsByPlatformIds(listOf(platformId))?.get(platformId.platform)?.get(platformId.platformId)
    }

    fun getPluginVersions(platformId: PlatformId, limit: Int = MAX_PLATFORM_VERSION_PAGE_SIZE): Array<Version>? {
        val pageSize = limit.coerceIn(1, MAX_PLATFORM_VERSION_PAGE_SIZE)
        val versions = mutableListOf<Version>()
        var offset = 0
        var pagesFetched = 0

        while (pagesFetched < MAX_PLATFORM_VERSION_PAGES) {
            val response = orpcCall(
                "/versions/platform/${platformId.platform.name.lowercase()}/${platformId.platformId}",
                params = mapOf(
                    "limit" to pageSize.toString(),
                    "offset" to offset.toString()
                )
            )

            if (!response.isSuccessful()) {
                logRequestFailure("Plugin version lookup", response)
                return versions.takeIf { it.isNotEmpty() }?.toTypedArray()
            }

            val page = runCatching {
                GSON.fromJson(response.body, ORPCPlatformVersionsResponse::class.java)
            }.onFailure {
                logParseFailure("Plugin version lookup", it)
            }.getOrNull() ?: return versions.takeIf { it.isNotEmpty() }?.toTypedArray()

            versions.addAll(page.versions.asList())
            pagesFetched++

            offset = nextPlatformVersionsOffset(page.pagination, offset, page.versions.size) ?: break
        }

        if (pagesFetched >= MAX_PLATFORM_VERSION_PAGES) {
            PluginPortalBase.plugin.logger.warning("Plugin version lookup stopped after $MAX_PLATFORM_VERSION_PAGES pages for ${platformId.platform} ${platformId.platformId}.")
        }

        return versions.toTypedArray()
    }

    fun getAllPluginsByPlatformIds(platformIds: List<PlatformId>): Map<MarketplacePlatform, Map<String, Plugin?>>? {
        val response = orpcCall("/plugins/platformids", body = GSON.toJson(platformIds.toTypedArray()))

        if (!response.isSuccessful()) {
            logRequestFailure("Platform plugin lookup", response)
            return null
        }

        val plugins = runCatching {
            GSON.fromJson(response.body, Array<Plugin>::class.java)
        }.onFailure {
            logParseFailure("Platform plugin lookup", it)
        }.getOrNull()

        if (plugins == null) return null

        val map = mutableMapOf<MarketplacePlatform, MutableMap<String, Plugin?>>()
        platformIds.forEach { (id, platform) ->
            map.getOrPut(platform, ::mutableMapOf)[id] = plugins.find { it.platform(platform)?.platformId == id }
        }
        return map
    }


    private val entryPluginMapToken = object: TypeToken<Map<String, Plugin?>>() {}

    /**
     * @return Returns a map of the entry ID used for the lookup, to the plugin it returned
     */
    fun getAllPluginsByEntryId(entries: EntryIdMap): Map<String, Plugin?>? {
        val response = orpcCall("plugins/ids", body = GSON.toJson(entries), params = mapOf("bukkitOnly" to "true"))

        if (!response.isSuccessful()) {
            logRequestFailure("Installed plugin lookup", response)
            return null
        }

        return runCatching {
            GSON.fromJson(response.body, entryPluginMapToken)
        }.onFailure {
            logParseFailure("Installed plugin lookup", it)
        }.getOrNull()
    }

    fun createEditorRoom(): EditorRoomResponse? {
        val response = orpcCall("/premium/editor", body = "{}")

        return if (response.isSuccessful()) {
            try {
                GSON.fromJson(response.body, EditorRoomResponse::class.java)
            } catch (e: Exception) {
                logParseFailure("Editor room creation", e)
                null
            }
        } else {
            logRequestFailure("Editor room creation", response, authSensitive = true)
            null
        }
    }

    fun createTemporaryServerLink(metadata: MCServerMetadata): TemporaryServerLinkResponse? {
        val response = orpcCall("/servers/temp", body = GSON.toJson(mapOf("body" to metadata)))

        return if (response.isSuccessful()) {
            runCatching {
                GSON.fromJson(response.body, TemporaryServerLinkResponse::class.java)
            }.onFailure {
                logParseFailure("Temporary server link creation", it)
            }.getOrNull()
        } else {
            logRequestFailure("Temporary server link creation", response)
            null
        }
    }

    fun createDeviceCode(): DeviceCodeResponse? {
        val url = "${HttpInfo.getApiBaseUrl()}/api/auth/device/code"
        val body = GSON.toJson(mapOf("client_id" to "plugin-portal-server", "scope" to "server"))

        return try {
            val request = Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                GSON.fromJson(response.body?.string() ?: "", DeviceCodeResponse::class.java)
            }
        } catch (e: Exception) {
            logParseFailure("Device code creation", e)
            null
        }
    }

    fun pollDeviceToken(deviceCode: String): DeviceTokenResponse? {
        val url = "${HttpInfo.getApiBaseUrl()}/api/auth/device/token"
        val body = GSON.toJson(mapOf(
            "grant_type" to "urn:ietf:params:oauth:grant-type:device_code",
            "device_code" to deviceCode,
            "client_id" to "plugin-portal-server"
        ))

        return try {
            val request = Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) return null
                GSON.fromJson(responseBody, DeviceTokenResponse::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun registerServer(metadata: MCServerMetadata, accessToken: String): RegisterServerResponse? {
        val response = orpcCall("/servers/register", body = GSON.toJson(mapOf("body" to metadata)), overrideAuth = accessToken)

        return if (response.isSuccessful()) {
            runCatching {
                GSON.fromJson(response.body, RegisterServerResponse::class.java)
            }.onFailure {
                logParseFailure("Server registration", it)
            }.getOrNull()
        } else {
            logRequestFailure("Server registration", response, authSensitive = true)
            null
        }
    }
    // Exception classes for backward compatibility
    private class AuthorisationException(val statusCode: Int) : RuntimeException(
        "The request was rejected by the remote server with status code $statusCode. You may need to authenticate."
    )

    private class PluginRequestFailedException(platform: MarketplacePlatform, id: String) : RuntimeException(
        "An unexpected error occurred when attempting to retrieve plugin $id from $platform. " + 
        "PLEASE REPORT THIS TO THE PLUGIN PORTAL AUTHORS"
    )
}

// Data classes for backward compatibility (these should already exist in the types package)
data class UpdateCheckResponse(
    val updateAvailable: Boolean,
    val current: VersionChannel,
    val latest: LatestVersion?,
)

data class VersionChannel(
    val version: String,
    val channel: String
)

data class LatestVersion(
    val version: String,
    val channel: String,
    val downloadUrl: String,
    val changelog: String?
)

data class EditorRoomResponse(
    val roomCode: String,
    val pluginOTP: String,
    val userOTP: String
)

data class MCServerMetadata(
    val name: String,
    val serverVersion: String? = null,
    val minecraftVersion: String? = null,
    val pluginVersion: String? = null,
    val capabilities: List<String> = listOf("LIST", "INSTALL", "UPDATE", "DELETE", "SETTINGS")
)

data class TemporaryServerLinkResponse(
    val code: String,
    val pluginToken: String,
    val browserToken: String,
    val url: String,
    val websocketUrl: String,
    val expiresInSeconds: Int
)

data class DeviceCodeResponse(
    val device_code: String,
    val user_code: String,
    val verification_uri: String,
    val verification_uri_complete: String,
    val expires_in: Int,
    val interval: Int
)

data class DeviceTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val scope: String?
)

data class RegisterServerResponse(
    val serverId: String,
    val apiKey: String,
    val websocketUrl: String
)

// Additional types for backward compatibility
data class VersionInfo(
    val version: String,
    val fullVersion: String,
    val channel: String,
    val stable: Boolean,
    val filename: String
)

data class PlatformId(
    val platformId: String,
    val platform: MarketplacePlatform,
) {
    fun matches(plugin: Plugin): Boolean = plugin.platform(platform)?.platformId == platformId
}
