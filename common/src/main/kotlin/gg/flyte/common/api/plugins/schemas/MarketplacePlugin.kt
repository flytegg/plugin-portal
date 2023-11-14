package gg.flyte.common.api.plugins.schemas

data class MarketplacePlugin(
    val id: String,
    val displayInfo: DisplayInfo,
    val stats: Stats,
    val primaryServiceType: ServiceType,
    val versionData: VersionData,
    var versions: HashMap<String, VersionInfo>, // HashMap<PlatformType, Hashmap<VersionID, VersionInfo>>
    val alternatePluginIds: HashSet<String>? // HashSet<Id>, this is where plugins are hosted on multiple services
) {
    fun getUniqueName() = "${displayInfo.name} (${id})"
    fun getDownloadCount() = stats.downloads
    fun getDownloadURL() = versions[versionData.latestVersion]?.downloadUrl
}