package gg.flyte.pluginportal.common.managers

import gg.flyte.pluginportal.common.PlatformId
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.GSON

internal const val LEGACY_ENTRY_ID_PREFIX = "legacy:"

internal data class LocalPluginCacheMigrationResult(
    val plugins: List<LocalPlugin>,
    val migratedCount: Int,
    val unresolvedCount: Int,
)

private data class StoredLocalPlugin(
    val entryId: String? = null,
    val platformId: String,
    val name: String,
    val version: String,
    val platform: MarketplacePlatform,
    val sha256: String,
    val sha512: String,
    val installedAt: Long,
    val preferredChannel: String? = null,
    val excludedFromUpdates: Boolean = false,
)

internal fun migrateLocalPluginCache(
    text: String,
    resolveEntryIds: (List<PlatformId>) -> Map<PlatformId, String>,
): LocalPluginCacheMigrationResult {
    val storedPlugins = GSON.fromJson(text, Array<StoredLocalPlugin>::class.java).toList()
    val identitiesToResolve = storedPlugins
        .filter { it.entryId.isNullOrBlank() || it.entryId.startsWith(LEGACY_ENTRY_ID_PREFIX) }
        .map { PlatformId(it.platformId, it.platform) }
        .distinct()
    val resolvedEntryIds = if (identitiesToResolve.isEmpty()) emptyMap() else resolveEntryIds(identitiesToResolve)

    var migratedCount = 0
    var unresolvedCount = 0
    val plugins = storedPlugins.map { stored ->
        val identity = PlatformId(stored.platformId, stored.platform)
        val existingEntryId = stored.entryId
            ?.takeIf(String::isNotBlank)
            ?.takeUnless { it.startsWith(LEGACY_ENTRY_ID_PREFIX) }
        val entryId = existingEntryId
            ?: resolvedEntryIds[identity]?.takeIf(String::isNotBlank)
            ?: "$LEGACY_ENTRY_ID_PREFIX${stored.platform.name.lowercase()}:${stored.platformId}".also {
                unresolvedCount++
            }

        if (entryId != stored.entryId) migratedCount++
        LocalPlugin(
            entryId = entryId,
            platformId = stored.platformId,
            name = stored.name,
            version = stored.version,
            platform = stored.platform,
            sha256 = stored.sha256,
            sha512 = stored.sha512,
            installedAt = stored.installedAt,
            preferredChannel = stored.preferredChannel,
            excludedFromUpdates = stored.excludedFromUpdates,
        )
    }

    return LocalPluginCacheMigrationResult(plugins, migratedCount, unresolvedCount)
}

internal fun LocalPlugin.hasResolvedEntryId(): Boolean =
    entryId.isNotBlank() && !entryId.startsWith(LEGACY_ENTRY_ID_PREFIX)
