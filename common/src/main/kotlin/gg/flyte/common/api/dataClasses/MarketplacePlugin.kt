package gg.flyte.common.api.dataClasses

import gg.flyte.common.type.service.PlatformType
import gg.flyte.common.type.service.ServiceType
import java.util.TreeSet

data class MarketplacePlugin(
    val id: String,
    val displayInfo: DisplayInfo,
    val stats: Stats,
    val service: ServiceType,
    val versionData: VersionData,
    val versions: HashMap<PlatformType, HashMap<String, VersionInfo>>,
)

data class VersionData(
    val releaseDate: String,
    val lastUpdated: String,
    val latestVersion: String,
)

data class Stats(
    val downloads: Int,
    val ratingAverage: Double,
    val ratingCount: Int,
    val isPremium: Boolean,
    val price: Double?,
)

data class VersionInfo(
    val downloadUrl: String,
    val shaHash: String?,
    val releaseDate: String,
    val supportedVersions: TreeSet<String> = TreeSet(),
    val dependencies: HashSet<Dependency> = hashSetOf(),
)

data class DisplayInfo(
    val name: String,
    val description: String,
    val iconURL: String,
    val extraInfo: String?,
)