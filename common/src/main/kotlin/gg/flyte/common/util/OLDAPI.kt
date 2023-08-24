package gg.flyte.common.util

import gg.flyte.common.api.dataClasses.MarketplacePlugin
import gg.flyte.common.exception.ServiceNotFoundException
import gg.flyte.common.type.error.PostError
import gg.flyte.common.type.plugin.RequestPlugin
import gg.flyte.common.type.service.ServiceType
//
//fun getPPVersions(): Versions = objectMapper.readValue(getStringFromURL("$BASE_DOMAIN/versions"), Versions::class.java)
//
///**
// * Retrieves the latest version of the plugin from the API.
// *
// * @return the latest version of the plugin
// */
//fun getLatestPPVersion(): String = getPPVersions().versions.entries.last().value
//
//fun convert(text: String, version: String): String = objectMapper.readValue(getStringFromURL("$BASE_DOMAIN/converter?text=$text&version=$version"), String::class.java)
//
//fun getPluginFromName(name: String) = objectMapper.readValue(getStringFromURL("$BASE_DOMAIN/v3/plugins/name/$name"), MarketplacePlugin::class.java) ?: null
//
//fun getPluginFromID(id: String) = objectMapper.readValue(getStringFromURL("$BASE_DOMAIN/v3/plugins/id/$id"), MarketplacePlugin::class.java) ?: null
//
//fun requestPlugin(requestPlugin: RequestPlugin): String = makePostRequest("$BASE_DOMAIN/v3/plugins", requestPlugin)
//
//fun sendError(postError: PostError): String = makePostRequest("$BASE_DOMAIN/errors", postError)
//
//fun getLatestVersionType(version: String): VersionType {
//    val latestVersion = getLatestPPVersion()
//
//    val currentVersionParts = version.split(".")
//    val latestVersionParts = latestVersion.split(".")
//
//    return when {
//        latestVersionParts[0].toInt() > currentVersionParts[0].toInt() -> VersionType.MAJOR
//        latestVersionParts[1].toInt() > currentVersionParts[1].toInt() -> VersionType.MINOR
//        latestVersionParts[2].toInt() > currentVersionParts[2].toInt() -> VersionType.PATCH
//        latestVersion == version -> VersionType.LATEST
//        else -> VersionType.UNRELEASED
//    }
//}
//
//fun separateServiceAndName(input: String): Pair<ServiceType, String> {
//    val (serviceStr, name) = input.split(":")
//    val service = when (serviceStr.lowercase()) {
//        "spigotmc" -> ServiceType.SPIGOTMC
//        "hangar" -> ServiceType.HANGAR
//        else -> throw ServiceNotFoundException()
//    }
//    return service to name
//}