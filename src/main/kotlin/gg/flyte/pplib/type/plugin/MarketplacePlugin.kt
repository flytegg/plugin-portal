package gg.flyte.pplib.type.plugin

import com.fasterxml.jackson.annotation.JsonProperty
import gg.flyte.pplib.util.getFinalRedirect
import gg.flyte.pplib.util.isJARFileDownload
import gg.flyte.pplib.util.requestPlugin

data class MarketplacePlugin(
    @JsonProperty("id")            val id: String,
    @JsonProperty("service")       val service: String,
    @JsonProperty("name")          val name: String,
    @JsonProperty("description")   val description: String,
    @JsonProperty("downloads")     val downloads: Int,
    @JsonProperty("price")         val price: Double,
    @JsonProperty("ratingAverage") val ratingAverage: Double,
    @JsonProperty("iconURL")       val iconURL: String,
    @JsonProperty("version")       var version: String,
    @JsonProperty("downloadURL")   val downloadURL: String,
    @JsonProperty("isPremium")     val isPremium: Boolean,
    @JsonProperty("extraInfo")     val extraInfo: String?,
) {
    fun isValidDownload(): Boolean {
        val validDownload = isJARFileDownload(getFinalRedirect(downloadURL))
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