package gg.flyte.pluginportal.backend.routing.routes.v1.plugins

import com.google.gson.annotations.SerializedName
import gg.flyte.pluginportal.api.type.*
import org.bson.codecs.pojo.annotations.BsonId

data class MongoPlugin(
    @BsonId
    @SerializedName("_id")
    val id: String,
    val displayInfo: DisplayInfo,
    val statistics: Statistics,
    val releaseData: ReleaseData,
    var versions: HashMap<String, PluginVersion>,
    val alternatePluginIds: HashSet<String>?
) {
    fun getUniqueName() = "${displayInfo.name} (${id})"
    fun getLatestVersion() = versions[releaseData.latestVersion]

    fun toCompactPlugin() = CompactPlugin(
        this.id,
        this.displayInfo.name,
        this.getLatestVersion()?.name,
    )

    fun toDto() = MarketplacePlugin(
        this.id,
        this.displayInfo,
        this.statistics,
        this.releaseData,
        this.versions,
        this.alternatePluginIds
    )
}