package gg.flyte.pluginportal.common.managers

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.types.Version
import gg.flyte.pluginportal.common.types.newestCompatibleVersionWithFallback
import gg.flyte.pluginportal.common.util.PP_PLUGIN_ID
import gg.flyte.pluginportal.common.util.currentMinecraftVersion
import gg.flyte.pluginportal.common.util.currentServerTypePreference
import gg.flyte.pluginportal.common.util.download
import gg.flyte.pluginportal.common.util.isPluginPortal
import net.kyori.adventure.audience.Audience

object PluginPortalSelfUpdateManager {
    const val DEFAULT_CHANNEL = "release"

    data class AvailableUpdate(val plugin: Plugin, val targetVersion: Version) {
        val versionString: String get() = targetVersion.versionNumber
        val channel: String get() = targetVersion.releaseChannel ?: "release"
    }

    fun fetchCanonicalPlugin(): Plugin? = API.getPluginById(PP_PLUGIN_ID)

    fun normalizeChannel(channel: String?): String =
        channel?.takeIf { it.isNotBlank() }
            ?.lowercase()
            ?.let { if (it == "stable") DEFAULT_CHANNEL else it }
            ?: DEFAULT_CHANNEL

    fun findMarketplaceTarget(channel: String? = null): AvailableUpdate? {
        val plugin = fetchCanonicalPlugin() ?: return null
        val platform = plugin.platforms.bestDownloadable ?: return null
        val serverTypes = currentServerTypePreference()
        val minecraftVersion = currentMinecraftVersion()
        val targetVersion = platform.newestCompatibleVersionWithFallback(normalizeChannel(channel), serverTypes, minecraftVersion) {
            API.getPluginVersions(platform.platformWithId)?.toList()
        } ?: return null
        return AvailableUpdate(plugin, targetVersion)
    }

    fun findMarketplaceUpdate(currentVersion: String, channel: String? = null): AvailableUpdate? {
        val update = findMarketplaceTarget(channel) ?: return null
        val targetVersion = update.targetVersion
        return if (isVersionNewer(targetVersion.versionNumber, currentVersion)) {
            update
        } else {
            null
        }
    }

    fun downloadMarketplaceUpdate(update: AvailableUpdate, audience: Audience? = null): Boolean {
        val newPlugin = update.plugin.download(
            update = true,
            marketplacePlatform = null,
            audience = audience,
            version = update.targetVersion,
            preferredChannel = update.targetVersion.releaseChannel,
        ) ?: return false

        return true
    }

    private fun isVersionNewer(candidateVersion: String, currentVersion: String): Boolean =
        comparePluginPortalVersions(candidateVersion, currentVersion) > 0
}

internal fun comparePluginPortalVersions(left: String, right: String): Int {
    val leftVersion = left.substringBefore("+")
    val rightVersion = right.substringBefore("+")
    fun String.baseParts() = substringBefore("-").split(Regex("[^0-9]+"))
        .filter(String::isNotBlank).map { it.toLongOrNull() ?: 0L }
    val leftParts = leftVersion.baseParts()
    val rightParts = rightVersion.baseParts()
    repeat(maxOf(leftParts.size, rightParts.size)) { index ->
        val diff = (leftParts.getOrNull(index) ?: 0L).compareTo(rightParts.getOrNull(index) ?: 0L)
        if (diff != 0) return diff
    }
    val leftPre = leftVersion.substringAfter("-", "").split('.').filter(String::isNotBlank)
    val rightPre = rightVersion.substringAfter("-", "").split('.').filter(String::isNotBlank)
    if (leftPre.isEmpty()) return if (rightPre.isEmpty()) 0 else 1
    if (rightPre.isEmpty()) return -1
    repeat(minOf(leftPre.size, rightPre.size)) { index ->
        val a = leftPre[index]
        val b = rightPre[index]
        val aNumber = a.toLongOrNull()
        val bNumber = b.toLongOrNull()
        val diff = when {
            aNumber != null && bNumber != null -> aNumber.compareTo(bNumber)
            aNumber != null -> -1
            bNumber != null -> 1
            else -> a.compareTo(b)
        }
        if (diff != 0) return diff
    }
    return leftPre.size.compareTo(rightPre.size)
}
