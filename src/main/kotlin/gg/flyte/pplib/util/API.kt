package gg.flyte.pplib.util

import com.fasterxml.jackson.databind.ObjectMapper
import gg.flyte.pplib.type.error.PostError
import gg.flyte.pplib.type.plugin.MarketplacePlugin
import gg.flyte.pplib.type.plugin.RequestPlugin
import gg.flyte.pplib.type.version.VersionType
import gg.flyte.pplib.type.version.Versions

//const val BASE_DOMAIN = "https://api.portalbox.link"
const val BASE_DOMAIN = "http://localhost:5005"

val objectMapper = ObjectMapper()

fun getPPVersions(): Versions = objectMapper.readValue(getStringFromURL("$BASE_DOMAIN/versions"), Versions::class.java)

/**
 * Retrieves the latest version of the plugin from the API.
 *
 * @return the latest version of the plugin
 */
fun getLatestPPVersion(): String = getPPVersions().versions.entries.last().value

fun convert(text: String, version: String): String = objectMapper.readValue(getStringFromURL("$BASE_DOMAIN/converter?text=$text&version=$version"), String::class.java)

fun getPluginFromName(name: String) = objectMapper.readValue(getStringFromURL("$BASE_DOMAIN/v3/plugins/name/$name"), MarketplacePlugin::class.java) ?: null

fun getPluginFromID(id: String) = objectMapper.readValue(getStringFromURL("$BASE_DOMAIN/v3/plugins/id/$id"), MarketplacePlugin::class.java) ?: null

fun requestPlugin(requestPlugin: RequestPlugin): String = makePostRequest("$BASE_DOMAIN/v2/plugins", requestPlugin)

fun sendError(postError: PostError): String = makePostRequest("$BASE_DOMAIN/errors", postError)

fun searchPlugins(filter: String): ArrayList<String> = objectMapper.readValue(getStringFromURL("$BASE_DOMAIN/v3/plugins/search?filter=${filter.alphaNumericOnly()}"), ArrayList<String>().javaClass)

fun hasUserStarredOnHangar(username: String): Boolean = objectMapper.readValue(getStringFromURL("$BASE_DOMAIN/hasUserStarredOnHangar?username=$username"), Boolean::class.java)

fun getLatestVersionType(version: String): VersionType {
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

/*
fun separateServiceAndName(input: String): Pair<ServiceType, String> {
    val (serviceStr, name) = input.split(":")
    val service = when (serviceStr.lowercase()) {
        "spigotmc" -> ServiceType.SPIGOTMC
        "hangar" -> ServiceType.HANGAR
        else -> throw ServiceNotFoundException()
    }
    return service to name
}
 */

fun startErrorCatcher(postError: PostError) {
    Thread.setDefaultUncaughtExceptionHandler { _, exception ->
        sendError(PostError(postError.pluginVersion, postError.minecraftVersion, exception.stackTraceToString()))
    }
}