package gg.flyte.common.type.api.plugin

import gg.flyte.common.api.dataClasses.Dependency
import gg.flyte.common.type.api.service.PlatformType
import gg.flyte.common.type.api.service.ServiceType
import java.util.TreeSet

data class MarketplacePlugin(
    val id: String,
    val displayInfo: DisplayInfo,
    val stats: Stats,
    val primaryServiceType: ServiceType,
    val versionData: VersionData,
    var versions: HashMap<PlatformType, HashMap<String, VersionInfo>>, // HashMap<PlatformType, Hashmap<VersionID, VersionInfo>>
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
    val supportedVersions: HashSet<String> = hashSetOf(),
    val dependencies: HashSet<Dependency> = hashSetOf(),
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