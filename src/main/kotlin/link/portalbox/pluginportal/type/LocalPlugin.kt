package link.portalbox.pluginportal.type

import com.fasterxml.jackson.annotation.JsonProperty
import gg.flyte.pplib.type.MarketplacePlugin

data class LocalPlugin(
        @JsonProperty("fileSha") var fileSha: String,
        @JsonProperty("marketplacePlugin") val marketplacePlugin: MarketplacePlugin
)