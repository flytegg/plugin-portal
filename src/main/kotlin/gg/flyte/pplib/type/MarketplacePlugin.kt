package gg.flyte.pplib.type

import com.fasterxml.jackson.annotation.JsonProperty
import gg.flyte.pplib.type.api.RequestPlugin
import gg.flyte.pplib.util.isJarFileDownload
import gg.flyte.pplib.util.requestPlugin

data class MarketplacePlugin(
    @JsonProperty("id") val id: String,
    @JsonProperty("service") val service: Service,

    // Plugin information
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("downloads") val downloads: Int,
    @JsonProperty("price") val price: Double,
    @JsonProperty("ratingAverage") val ratingAverage: Double,
    @JsonProperty("iconURL") val iconURL: String,

    // Service data
    @JsonProperty("version") val version: String,

    // Download information
    @JsonProperty("downloadURL") val downloadURL: String,
    @JsonProperty("isPremium") val isPremium: Boolean,
) {
    fun isValidDownload(): Boolean {
        val validDownload = isJarFileDownload(downloadURL)
        if (!validDownload) {
            requestPlugin(RequestPlugin(
                this.id,
                this.service,
                "Invalid Download URL",
                this.name,
                this.downloadURL,
                ""
            ))
        }

        return validDownload
    }
}
