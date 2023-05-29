package gg.flyte.pplib.util

import com.fasterxml.jackson.module.kotlin.readValue
import gg.flyte.pplib.type.MarketplacePlugin
import gg.flyte.pplib.type.Service
import gg.flyte.pplib.type.api.PostError
import gg.flyte.pplib.type.api.RequestPlugin
import gg.flyte.pplib.type.VersionType
import gg.flyte.pplib.type.api.Versions
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

// val BASE_DOMAIN = "https://api.portalbox.link"
val BASE_DOMAIN = "http://localhost:5005"

/**
 * Retrieves the latest version of the plugin from the API.
 *
 * @return the latest version of the plugin
 */
fun getLatestPPVersion(): String {
    return getPPVersions().versions.entries.last().value
}

fun getLatestVersion(version: String): VersionType {
    val latestVersion = getLatestPPVersion()

    val currentVersionParts = version.split(".")
    val latestVersionParts = latestVersion.split(".")

    return when {
        latestVersionParts[0].toInt() > currentVersionParts[0].toInt() -> VersionType.MAJOR
        latestVersionParts[1].toInt() > currentVersionParts[1].toInt() -> VersionType.MINOR
        latestVersionParts[2].toInt() > currentVersionParts[2].toInt() -> VersionType.PATCH
        latestVersion == version -> VersionType.LATEST
        else -> VersionType.UNRELEASED
    }
}

/**
 * Retrieves a map of PP versions from the API.
 *
 * @return a LinkedHashMap containing the plugin hash and its version.
 */
fun getPPVersions(): Versions {
    return objectMapper.readValue(getStringFromURL("$BASE_DOMAIN/versions"), Versions::class.java)
}

/**
 * Retrieves the plugin JSON from the API.
 * @param name The ID of the plugin to retrieve.
 * @return an APIPlugin object representing the specified id
 */
fun getPluginFromName(name: String): MarketplacePlugin? {
    return objectMapper.readValue<MarketplacePlugin>(getStringFromURL("$BASE_DOMAIN/v3/plugins/name/$name")) ?: null
}

/**
 * Retrieves the plugin JSON from the API.
 * @param id The ID of the plugin to retrieve.
 * @return an APIPlugin object representing the specified id
 */
fun getPluginFromId(id: String): MarketplacePlugin? {
    println("$BASE_DOMAIN/v3/plugins/id/$id")
    return objectMapper.readValue<MarketplacePlugin>(getStringFromURL("$BASE_DOMAIN/v3/plugins/id/$id")) ?: null
}

/**
 * Requests a plugin from the developers via a RestAPI post request.
 * @param requestPlugin The plugin to request.
 */
fun requestPlugin(requestPlugin: RequestPlugin): String {
    val client = okHttpClient
    val request = Request.Builder()
        .url("$BASE_DOMAIN/v3/plugins")
        .method(
            "POST",
            objectMapper.writeValueAsString(requestPlugin).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        )
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        return response.body.string()
    }
}

fun sendError(postError: PostError): String {
    val client = okHttpClient
    val request = Request.Builder()
        .url("$BASE_DOMAIN/errors")
        .method(
            "POST",
            objectMapper.writeValueAsString(postError).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        )
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        return response.body.string()
    }
}

fun searchPlugins(filter: String): ArrayList<String> {
    return objectMapper.readValue(getStringFromURL("$BASE_DOMAIN/v3/plugins/search?filter=$filter"), ArrayList<String>().javaClass)
}

fun separateServiceAndName(input: String): Pair<Service, String> {
    println(input)
    val parts = input.split(":")
    println(parts)
    val service = when (parts.first().lowercase()) {
        "spigotmc" -> Service.SPIGOTMC
        "hangar" -> Service.HANGAR
        // Add more cases for other marketplace services if needed
        else -> throw IllegalArgumentException("Invalid marketplace service: ${parts.first()}")
    }
    val name = parts.drop(1).joinToString(":")
    return Pair(service, name)
}

fun startErrorCatcher(postError: PostError) {
    defaultPostError = postError
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        e.printStackTrace()
        sendError(
            PostError(
                defaultPostError.pluginVersion,
                defaultPostError.mcVersion,
                e.stackTraceToString(),
            )
        )
    }
}