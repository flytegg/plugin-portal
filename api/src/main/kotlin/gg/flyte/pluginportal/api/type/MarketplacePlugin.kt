package gg.flyte.pluginportal.api.type

data class MarketplacePlugin(
    val id: String,
    val displayInfo: DisplayInfo,
    val statistics: Statistics,
    val releaseData: ReleaseData,
    var versions: HashMap<String, PluginVersion>, // HashMap<PlatformType, Hashmap<VersionID, VersionInfo>>
    val alternatePluginIds: HashSet<String>? // HashSet<Id>, this is where plugins are hosted on multiple services
) {
    fun getUniqueName() = "${displayInfo.name} (${id})"
    fun getLatestVersion() = versions[releaseData.latestVersion]

    fun toCompactPlugin() = CompactPlugin(
        this.id,
        this.displayInfo.name,
        this.getLatestVersion()?.name,
    )
}