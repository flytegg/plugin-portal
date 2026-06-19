package gg.flyte.pluginportal.common.managers

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.types.Version
import gg.flyte.pluginportal.common.util.PP_PLUGIN_ID
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
        val targetVersion = platform.newestCompatibleVersion(normalizeChannel(channel), currentServerTypePreference()) ?: return null
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
        val oldPlugin = LocalPluginCache.firstOrNull { it.isPluginPortal }
        val newPlugin = update.plugin.download(
            update = true,
            marketplacePlatform = null,
            audience = audience,
            version = update.targetVersion,
            preferredChannel = update.targetVersion.releaseChannel,
        ) ?: return false

        if (oldPlugin != null) LocalPluginCache.addToUpdatedPluginMap(newPlugin, oldPlugin)
        return true
    }

    private fun isVersionNewer(candidateVersion: String, currentVersion: String): Boolean =
        compareVersions(candidateVersion, currentVersion.substringBefore("-")) > 0

    private fun compareVersions(left: String, right: String): Int {
        val leftParts = left.versionParts()
        val rightParts = right.versionParts()
        val max = maxOf(leftParts.size, rightParts.size)
        repeat(max) { index ->
            val diff = (leftParts.getOrNull(index) ?: 0) - (rightParts.getOrNull(index) ?: 0)
            if (diff != 0) return diff
        }
        return 0
    }

    private fun String.versionParts(): List<Int> =
        substringBefore("-")
            .split(Regex("[^0-9]+"))
            .filter(String::isNotBlank)
            .map { it.toIntOrNull() ?: 0 }
}
