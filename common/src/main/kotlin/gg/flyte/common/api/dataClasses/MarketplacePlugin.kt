package gg.flyte.common.api.dataClasses

import gg.flyte.common.type.api.service.PlatformGroup
import gg.flyte.common.type.api.service.PlatformType
import gg.flyte.common.type.api.service.ServiceType
import java.util.TreeSet

data class MarketplacePlugin(
    val id: String,
    val displayInfo: DisplayInfo,
    val stats: Stats,
    val primaryServiceType: ServiceType,
    val versionData: VersionData,
    val versions: HashMap<PlatformType, HashMap<String, VersionInfo>>,
    val alternatePluginIds: HashSet<String>? // HashSet<Id>, this is where plugins are hosted on multiple services
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