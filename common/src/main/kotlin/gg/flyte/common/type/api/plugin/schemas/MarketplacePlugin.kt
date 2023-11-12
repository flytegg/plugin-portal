package gg.flyte.common.type.api.plugin.schemas

data class MarketplacePlugin(
    val id: String,
    val displayInfo: DisplayInfo,
    val stats: Stats,
    val primaryServiceType: ServiceType,
    val versionData: VersionData,
    var versions: HashMap<String, VersionInfo>, // HashMap<PlatformType, Hashmap<VersionID, VersionInfo>>
    val alternatePluginIds: HashSet<String>? // HashSet<Id>, this is where plugins are hosted on multiple services
) {
    fun getDownloadCount() = stats.downloads
}