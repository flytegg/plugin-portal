package gg.flyte.pluginportal.common.types

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.PlatformId
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.types.enums.ServerType
import gg.flyte.pluginportal.common.util.currentMinecraftVersion
import gg.flyte.pluginportal.common.util.currentServerTypePreference

/** Represents a plugin that is installed on the local server. */
data class LocalPlugin(
    val entryId: String,
    val platformId: String,
    val name: String,
    val version: String,
    val platform: MarketplacePlatform,
    val sha256: String,
    val sha512: String,
    val installedAt: Long,
    var preferredChannel: String? = null,
    var excludedFromUpdates: Boolean = false
) {
    private inner class LocalPluginException(num: Int, msg: String) : Exception("$msg @ $name ($platform $platformId) ($num)")

    val platformWithId get() = PlatformId(platformId, platform)
    val marketplacePlugin: Plugin get() = MarketplacePluginCache.getCachedPluginById(platform, platformId)
        ?: MarketplacePluginCache.getOrFetchPluginById(platform, platformId)
        ?: throw LocalPluginException(1, "Could not retrieve plugin")

    val isUpToDate: Boolean get() {
        return targetUpdateVersion() == null
    }

    fun targetUpdateVersion(plugin: Plugin = marketplacePlugin): Version? {
        val platformPlugin = plugin.platform(platform) ?: return null
        return targetUpdateVersion(platformPlugin, currentServerTypePreference(), currentMinecraftVersion()) {
            API.getPluginVersions(platformPlugin.platformWithId)?.toList()
        }
    }

    internal fun targetUpdateVersion(
        platformPlugin: PlatformPlugin,
        serverTypes: List<ServerType>,
        minecraftVersion: String?,
        fetchVersions: () -> List<Version>?,
    ): Version? {
        val cachedTarget = platformPlugin.newestCompatibleVersion(preferredChannel, serverTypes, minecraftVersion)

        if (
            cachedTarget != null
            && cachedTarget.hasResolvedChannel(preferredChannel)
            && isNewerThanInstalled(cachedTarget)
            && cachedTarget.bestServerTypeRank(serverTypes) == 0
            && (minecraftVersion == null || cachedTarget.explicitlySupportsMinecraftVersion(minecraftVersion))
        ) return cachedTarget

        val fullVersions = fetchVersions()
        val target = if (fullVersions != null) {
            fullVersions.newestCompatibleVersion(preferredChannel, serverTypes, minecraftVersion)
        } else {
            cachedTarget?.takeIf { it.hasResolvedChannel(preferredChannel) }
        }
        return target?.takeIf(::isNewerThanInstalled)
    }

    fun matchesVersion(target: Version): Boolean =
        if (!target.sha256.isNullOrBlank()) sha256.equals(target.sha256, ignoreCase = true)
        else version == target.versionNumber

    private fun isNewerThanInstalled(target: Version): Boolean {
        if (matchesVersion(target)) return false
        val comparison = compareCoreVersions(target.versionNumber, version)
        return comparison == null || comparison > 0
    }

    private fun comparableVersionParts(version: String): List<Int> {
        val coreVersion = version.substringBefore('+').substringBefore('-')
        val match = Regex("""\d+(?:\.\d+)*""").find(coreVersion) ?: return emptyList()
        return match.value
            .split(".")
            .mapNotNull { it.toIntOrNull() }
    }

    private fun compareCoreVersions(left: String, right: String): Int? {
        val leftParts = comparableVersionParts(left)
        val rightParts = comparableVersionParts(right)
        if (leftParts.isEmpty() || rightParts.isEmpty()) return null

        val length = maxOf(leftParts.size, rightParts.size)
        for (index in 0 until length) {
            val leftPart = leftParts.getOrElse(index) { 0 }
            val rightPart = rightParts.getOrElse(index) { 0 }
            if (leftPart != rightPart) return leftPart.compareTo(rightPart)
        }

        return 0
    }

}
