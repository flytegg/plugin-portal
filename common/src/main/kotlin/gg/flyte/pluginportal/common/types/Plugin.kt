package gg.flyte.pluginportal.common.types

import com.google.gson.annotations.SerializedName
import gg.flyte.db.MarketplacePlatform

data class Plugin(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val platforms: MutableMap<MarketplacePlatform, PlatformPlugin>,
)

data class PlatformPlugin(
    val id: String,
    val name: String,
    val description: String?,
    val downloads: Int,
    val imageURL: String?,
    val lastUpdated: String?,
)