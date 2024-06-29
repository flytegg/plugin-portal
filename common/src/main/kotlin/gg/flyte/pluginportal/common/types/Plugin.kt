package gg.flyte.pluginportal.common.types

import com.google.gson.annotations.SerializedName

data class Plugin(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val platforms: MutableMap<MarketplacePlatform, PlatformPlugin>,
) {
    val isPluginPortal: Boolean = platforms[MarketplacePlatform.MODRINTH]?.id == "5qkQnnWO" // can add premium here too
    val highestPriorityPlatform get() = MarketplacePlatform.entries.find(platforms::containsKey) ?: platforms.keys.first()

    fun getFirstPlatform(): PlatformPlugin? = platforms.values.firstOrNull()

    fun getImageURL(): String? = platforms.values
        .firstOrNull {
            it.imageURL?.contains(".png") == true || it.imageURL?.contains(".jpg") == true
        }?.imageURL ?: platforms.values.firstOrNull()?.imageURL

    fun getDescription(): String? = getFirstPlatform()?.description?.replace("\n", " ")

    val totalDownloads: Int get() = platforms.values.sumOf { platform -> platform.downloads }

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