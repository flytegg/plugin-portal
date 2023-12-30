package gg.flyte.pluginportal.scanner

import com.google.gson.annotations.SerializedName
import gg.flyte.pluginportal.api.type.*
import org.bson.codecs.pojo.annotations.BsonId

data class MongoPlugin(
    @BsonId
    val id: String,
    val displayInfo: DisplayInfo,
    val statistics: Statistics,
    val releaseData: ReleaseData,
    var versions: HashMap<String, PluginVersion>,
    val alternatePluginIds: HashSet<String>?
)