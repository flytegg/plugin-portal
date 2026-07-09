package gg.flyte.pluginportal.common.types

import com.google.gson.annotations.SerializedName
import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.PlatformId
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.types.enums.ServerPlatform
import gg.flyte.pluginportal.common.types.enums.ServerType
import java.util.*
import kotlin.collections.find

// The root response object for a plugin search
data class PluginSearchResponse(
    val plugins: List<Plugin>, val pagination: Pagination
)

// Represents a single plugin in the search results
data class Plugin(
    @SerializedName("_id") val id: String,
    val name: String,
    val totalDownloads: Int,
    val platforms: Platforms,
    val createdAt: Date,
    val updatedAt: Date
) {
    val downloadableName get() = name.replace(Regex("[/\\\\]"), "")
    fun getFullDownloadedName(platform: MarketplacePlatform) = "[PP] $downloadableName ($platform).jar"
    val sanitisedDescription get() = platforms.bestDownloadable?.description?.replace("\n", " ") ?: platforms.best?.description?.replace("\n", " ")
    val bestPlatform get() = platforms.bestDownloadable?.platform
    val iconURL
        get() = platforms.asList()
            .find { listOf(".jpg", ".png").any { ext -> it.iconURL?.contains(ext) == true } }?.iconURL
            ?: platforms.best?.iconURL

    fun isParentOf(local: LocalPlugin): Boolean = local.platformId == platform(local.platform)?.platformId

    fun platform(platform: MarketplacePlatform): PlatformPlugin? = platforms.let {
        when (platform) {
            MarketplacePlatform.MODRINTH -> it.modrinth
            MarketplacePlatform.HANGAR -> it.hangar
            MarketplacePlatform.SPIGOTMC -> it.spigotmc
            MarketplacePlatform.POLYMART -> it.polymart
        }
    }
}

// Container for the different platform-specific entries
data class Platforms(
    val modrinth: ModrinthPlatformEntry?,
    val spigotmc: SpigotMCPlatformEntry?,
    val hangar: HangarPlatformEntry?,
    val polymart: PolymartPlatformEntry?
) {
    val best get() = modrinth ?: polymart ?: hangar ?: spigotmc
    val bestDownloadable get() = asList().firstOrNull { Config.isDownloadPlatformEnabled(it.platform) }
    val available get() = asList().map(PlatformPlugin::platform)
    val availableDownloadPlatforms get() = asList()
        .filter { Config.isDownloadPlatformEnabled(it.platform) }
        .map(PlatformPlugin::platform)

    fun byEntryId(id: String): PlatformPlugin? = when (id) {
        modrinth?.entryId -> modrinth
        spigotmc?.entryId -> spigotmc
        hangar?.entryId -> hangar
        polymart?.entryId -> polymart
        else -> null
    }

    fun asList(): List<PlatformPlugin> = arrayOf(modrinth, hangar, spigotmc, polymart).filterNotNull()
    fun has(platform: MarketplacePlatform): Boolean = available.contains(platform)
}

abstract class PlatformPlugin(
    @SerializedName("_id")
    val entryId: String,
    val platform: MarketplacePlatform,
    val platformId: String,
    val author: String,
    val description: String?,
    val iconURL: String?,
    val downloads: Int,
    @SerializedName(value = "lastModified", alternate = ["lastUpdated"])
    val lastModified: Date?,
    val versions: List<Version>
) {
    // TODO: People will be able to CONFIGURE whether or not they want to use alpha/beta etc. Calculate this variable based on that config.
    val latestVersion get() = versions.getOrNull(0) ?: throw NullPointerException("$platformId has no available versions (1)")
    // TODO: Add a way to configure, per-plugin, whether or not this should be paper/bukkit ONLY (FAWE Paper, per e.g.)
    val latestBukkitPaperVersion get() = versions.find(Version::isBukkitPaper) ?: throw NullPointerException("$platformId has no available BUKKIT versions (2)")
    val bukkitPaperVersions get() = versions.filter { it.isBukkitPaper }
    val platformWithId get() = PlatformId(platformId, platform)

    fun compatibleVersions(serverTypePreference: List<ServerType>): List<Version> =
        versions.compatibleVersions(serverTypePreference)

    fun compatibleVersions(serverTypePreference: List<ServerType>, minecraftVersion: String?): List<Version> =
        versions.compatibleVersions(serverTypePreference, minecraftVersion)

    fun newestCompatibleVersion(channel: String?, serverTypePreference: List<ServerType>, minecraftVersion: String? = null): Version? =
        versions.newestCompatibleVersion(channel, serverTypePreference, minecraftVersion)

    fun exactCompatibleVersion(versionNumber: String, channel: String?, serverTypePreference: List<ServerType>, minecraftVersion: String? = null): ExactVersionSelection {
        return versions.exactCompatibleVersion(versionNumber, channel, serverTypePreference, minecraftVersion)
    }

    fun newestBukkitPaperVersion(channel: String?): Version? =
        bukkitPaperVersions.firstOrNull { channel == null || it.releaseChannel.equals(channel, ignoreCase = true) }

    fun bukkitPaperVersion(versionNumber: String): Version? =
        bukkitPaperVersions.firstOrNull { it.versionNumber == versionNumber }

    abstract val webpageURL: String
}

sealed class ExactVersionSelection {
    data class Found(val version: Version) : ExactVersionSelection()
    data class Ambiguous(val channels: List<String>) : ExactVersionSelection()
    object NotFound : ExactVersionSelection()
}

fun List<Version>.compatibleVersions(serverTypePreference: List<ServerType>, minecraftVersion: String? = null): List<Version> =
    filter { version -> version.isCompatibleWith(serverTypePreference) }
        .preferMinecraftVersion(minecraftVersion)

fun List<Version>.exactCompatibleVersion(versionNumber: String, channel: String?, serverTypePreference: List<ServerType>, minecraftVersion: String? = null): ExactVersionSelection {
    val matches = filter { version -> version.isCompatibleWith(serverTypePreference) }
        .filter { version -> version.versionNumber == versionNumber }
        .filter { version -> channel == null || version.releaseChannel.equals(channel, ignoreCase = true) }
        .preferMinecraftVersion(minecraftVersion)

    if (matches.isEmpty()) return ExactVersionSelection.NotFound

    val channels = matches
        .map { version -> version.releaseChannel ?: "default" }
        .distinctBy { versionChannel -> versionChannel.lowercase() }
        .sorted()

    if (channel == null && channels.size > 1) return ExactVersionSelection.Ambiguous(channels)

    return ExactVersionSelection.Found(matches.bestCompatibleVersion(serverTypePreference)!!)
}

private fun List<Version>.bestCompatibleVersion(serverTypePreference: List<ServerType>): Version? =
    minWithOrNull(
        compareBy<Version> { version ->
            version.bestServerTypeRank(serverTypePreference)
        }.thenByDescending { version ->
            version.releasedAt
        }
    )

fun List<Version>.newestCompatibleVersion(channel: String?, serverTypePreference: List<ServerType>, minecraftVersion: String? = null): Version? =
    filter { version -> version.isCompatibleWith(serverTypePreference) }
        .filter { version -> channel == null || version.releaseChannel.equals(channel, ignoreCase = true) }
        .preferMinecraftVersion(minecraftVersion)
        .bestCompatibleVersion(serverTypePreference)

fun PlatformPlugin.newestCompatibleVersionWithFallback(
    channel: String?,
    serverTypePreference: List<ServerType>,
    minecraftVersion: String? = null,
    fetchVersions: () -> List<Version>?
): Version? =
    newestCompatibleVersion(channel, serverTypePreference, minecraftVersion)
        ?.takeIf { version -> version.isSpecificMatch(serverTypePreference, minecraftVersion) }
        ?: fetchVersions()?.newestCompatibleVersion(channel, serverTypePreference, minecraftVersion)
        ?: newestCompatibleVersion(channel, serverTypePreference, minecraftVersion)

fun PlatformPlugin.exactCompatibleVersionWithFallback(
    versionNumber: String,
    channel: String?,
    serverTypePreference: List<ServerType>,
    minecraftVersion: String? = null,
    fetchVersions: () -> List<Version>?
): ExactVersionSelection {
    val cached = exactCompatibleVersion(versionNumber, channel, serverTypePreference, minecraftVersion)
    if (cached is ExactVersionSelection.Found && cached.version.isSpecificMatch(serverTypePreference, minecraftVersion)) {
        return cached
    }

    return when (val full = fetchVersions()?.exactCompatibleVersion(versionNumber, channel, serverTypePreference, minecraftVersion)) {
        null, ExactVersionSelection.NotFound -> cached
        else -> full
    }
}

private fun List<Version>.preferMinecraftVersion(minecraftVersion: String?): List<Version> {
    val normalized = normalizeMinecraftVersion(minecraftVersion) ?: return this
    val explicitMatches = filter { version -> version.explicitlySupportsMinecraftVersion(normalized) }
    if (explicitMatches.isNotEmpty()) return explicitMatches

    val unknownCompatibility = filter { version -> version.supportedMinecraftVersions.isEmpty() }
    return unknownCompatibility.ifEmpty { this }
}

private fun Version.isSpecificMatch(serverTypePreference: List<ServerType>, minecraftVersion: String?): Boolean {
    val exactServerType = bestServerTypeRank(serverTypePreference) == 0
    val normalizedMinecraftVersion = normalizeMinecraftVersion(minecraftVersion)
    return exactServerType && (normalizedMinecraftVersion == null || explicitlySupportsMinecraftVersion(normalizedMinecraftVersion))
}

// Detailed entry for a plugin on Modrinth
class ModrinthPlatformEntry(
    entryId: String,
    platform: MarketplacePlatform,
    platformId: String,
    author: String,
    description: String?,
    iconURL: String?,
    downloads: Int,
    lastModified: Date?,
    versions: List<Version>,
    val followers: Int,
    val lastSynced: Date,
) : PlatformPlugin(entryId, platform, platformId, author, description, iconURL, downloads, lastModified, versions) {
    override val webpageURL: String get() = "https://modrinth.com/plugin/${platformId}"
}

// Detailed entry for a plugin on SpigotMC
class SpigotMCPlatformEntry(
    entryId: String,
    platform: MarketplacePlatform,
    platformId: String,
    author: String,
    description: String?,
    iconURL: String?,
    downloads: Int,
    lastModified: Date?,
    versions: List<Version>,
    val rating: SpigotMCRating,
) : PlatformPlugin(entryId, platform, platformId, author, description, iconURL, downloads, lastModified, versions) {
    override val webpageURL: String get() = "https://www.spigotmc.org/resources/${platformId}"
}

class PolymartPlatformEntry(
    entryId: String,
    platform: MarketplacePlatform,
    platformId: String,
    author: String,
    description: String?,
    iconURL: String?,
    downloads: Int,
    lastModified: Date?,
    versions: List<Version>,

    val reviews: PolymartRating,
    val premium: PolymartPremium?
) : PlatformPlugin(entryId, platform, platformId, author, description, iconURL, downloads, lastModified, versions) {
    override val webpageURL: String get() = "https://polymart.org/product/${platformId}&ref=41023"
}

class HangarPlatformEntry(
    entryId: String,
    platform: MarketplacePlatform,
    platformId: String,
    author: String,
    description: String?,
    iconURL: String?,
    downloads: Int,
    lastModified: Date?,
    versions: List<Version>,
    val stars: Int,
) : PlatformPlugin(entryId, platform, platformId, author, description, iconURL, downloads, lastModified, versions) {
    override val webpageURL: String get() = "https://hangar.papermc.io/${author}/${platformId}"
}

data class SpigotMCRating(
    val count: Int, val average: Double
)

data class PolymartRating(
    val count: Int, val stars: Double
)

data class PolymartPremium(
    val price: Double, val currency: String
)

// Represents a single version of a plugin
data class Version(
    val versionNumber: String,
    val releasedAt: Date,
    @SerializedName("channel")
    val releaseChannel: String?,
    val downloadURL: String?,
    val supportedVersions: String?,
    @SerializedName("mcVersions")
    val mcVersions: List<String>? = null,
    val serverTypes: Array<ServerType>,
    val sha256: String?,
) {
    val supportedMinecraftVersions: List<String>
        get() = (extractMinecraftVersions(supportedVersions) + mcVersions.orEmpty().mapNotNull(::normalizeMinecraftVersion))
            .distinct()

    val serverPlatforms: Set<ServerPlatform> get() = serverTypes?.map { it.platform }?.toSet() ?: throw NullPointerException("serverTypes array is null!")
    val isBukkitPaper get() = serverPlatforms.let { it.contains(ServerPlatform.BUKKIT) || it.contains(ServerPlatform.PAPER) }

    fun isCompatibleWith(serverTypePreference: List<ServerType>): Boolean =
        bestServerTypeRank(serverTypePreference) != Int.MAX_VALUE

    fun explicitlySupportsMinecraftVersion(minecraftVersion: String?): Boolean {
        val normalized = normalizeMinecraftVersion(minecraftVersion) ?: return false
        return supportedMinecraftVersions.any { supported -> minecraftVersionsMatch(supported, normalized) }
    }

    fun bestServerTypeRank(serverTypePreference: List<ServerType>): Int {
        val availableTypes = serverTypes.toSet()
        val exactServerType = serverTypePreference.firstOrNull()
        if (exactServerType != null && exactServerType in availableTypes) return 0
        val fallbackPreference = serverTypePreference.drop(1)
        val bukkitFallbackRank = fallbackPreference.indexOfFirst { it.platform == ServerPlatform.BUKKIT }
            .takeIf { it >= 0 }
            ?.plus(1)

        fallbackPreference.forEachIndexed { index, preferred ->
            if (preferred in availableTypes) {
                return if (preferred.platform == ServerPlatform.BUKKIT) bukkitFallbackRank ?: index + 1 else index + 1
            }
        }

        return Int.MAX_VALUE
    }
}

private val minecraftVersionPattern = Regex("""\b\d+\.\d+(?:\.\d+)?\b""")

private fun extractMinecraftVersions(value: String?): List<String> =
    value
        ?.let { minecraftVersionPattern.findAll(it).mapNotNull { match -> normalizeMinecraftVersion(match.value) }.toList() }
        ?: emptyList()

private fun normalizeMinecraftVersion(value: String?): String? =
    value
        ?.let { minecraftVersionPattern.find(it)?.value }
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

private fun minecraftVersionsMatch(supported: String, target: String): Boolean {
    if (supported == target) return true
    val supportedParts = supported.split(".")
    val targetParts = target.split(".")
    if (supportedParts.size == 2 && targetParts.size >= 2) {
        return supportedParts[0] == targetParts[0] && supportedParts[1] == targetParts[1]
    }
    if (targetParts.size == 2 && supportedParts.size >= 2) {
        return supportedParts[0] == targetParts[0] && supportedParts[1] == targetParts[1]
    }
    return false
}

// Represents the file hashes for a version
data class Hashes(
    val sha256: String?, val sha512: String?
)

// Holds pagination metadata for the search results
data class Pagination(
    val total: Int, val limit: Int, val offset: Int, val hasMore: Boolean
)
