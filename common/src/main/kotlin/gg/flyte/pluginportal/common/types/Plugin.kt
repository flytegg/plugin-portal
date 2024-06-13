package gg.flyte.pluginportal.common.types

import com.google.gson.annotations.SerializedName
import gg.flyte.db.MarketplacePlatform

data class Plugin(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val platforms: MutableMap<MarketplacePlatform, PlatformPlugin>,
) {
    fun getFirstPlatform(): PlatformPlugin? = platforms.values
            .firstOrNull()

    fun getImageURL(): String? = getFirstPlatform()?.imageURL

    fun getDescription(): String? = getFirstPlatform()?.description

    fun getDownloads(): Int = platforms.values
        .sumOf { platform -> platform.downloads }

}

data class PlatformPlugin(
    val id: String,
    val name: String,
    val author: String?,
    val description: String?,
    val downloads: Int,
    val imageURL: String?,
    val download: PlatformDownload?,
    val lastUpdated: String?,
)

data class PlatformDownload(
    val url: String,
    val version: String,
)