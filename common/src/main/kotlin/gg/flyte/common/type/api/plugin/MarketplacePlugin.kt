package gg.flyte.common.type.api.plugin

import gg.flyte.common.api.dataClasses.Dependency
data class MarketplacePlugin(
    val id: String,
    val displayInfo: DisplayInfo,
    val stats: Stats,
    val primaryServiceType: String,
    val versionData: VersionData,
    var versions: HashMap<String, VersionInfo>, // HashMap<PlatformType, Hashmap<VersionID, VersionInfo>>
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
    val name: String,
    val downloadUrl: String?,
    val hashes: HashMap<String, String>,
    val releaseDate: String,
    val supportedVersionsRange: String?,
    val dependencies: HashSet<Dependency>?,
    val malwareInfo: MalwareInfo?,
)

data class DisplayInfo(
    val name: String,
    val description: String,
    val iconURL: String,
    val extraInfo: String?,
)

data class MalwareInfo(
    val reason: String?,
    val scannerUsed: String,
    val scanDate: Long,
    val scanResult: HashMap<String, Double>, // MalwareType, ConfidenceRate
    val conclusion: Boolean,
)