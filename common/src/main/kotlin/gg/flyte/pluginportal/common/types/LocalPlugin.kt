package gg.flyte.pluginportal.common.types

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.PlatformId
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
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
        val target = targetUpdateVersion()
        if (target == null) {
            PluginPortalBase.plugin.logger.warning("No compatible versions for $name ($platform $platformId)")
            return false
        }
        return matchesVersion(target)
    }

    fun targetUpdateVersion(plugin: Plugin = marketplacePlugin): Version? {
        val platformPlugin = plugin.platform(platform) ?: return null
        val serverTypes = currentServerTypePreference()
        val minecraftVersion = currentMinecraftVersion()
        val cachedTarget = platformPlugin.newestCompatibleVersion(preferredChannel, serverTypes, minecraftVersion)

        if (
            cachedTarget != null
            && !matchesVersion(cachedTarget)
            && cachedTarget.bestServerTypeRank(serverTypes) == 0
            && (minecraftVersion == null || cachedTarget.explicitlySupportsMinecraftVersion(minecraftVersion))
        ) return cachedTarget

        val fullTarget = API.getPluginVersions(platformPlugin.platformWithId)
            ?.toList()
            ?.newestCompatibleVersion(preferredChannel, serverTypes, minecraftVersion)

        return fullTarget ?: cachedTarget
    }

    fun matchesVersion(target: Version): Boolean =
        if (!target.sha256.isNullOrBlank()) sha256.equals(target.sha256, ignoreCase = true)
        else version == target.versionNumber

}
