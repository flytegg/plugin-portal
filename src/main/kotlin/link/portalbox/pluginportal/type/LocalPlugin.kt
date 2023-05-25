package link.portalbox.pluginportal.type

import com.fasterxml.jackson.annotation.JsonProperty
import gg.flyte.pplib.type.Service

data class LocalPlugin(
        @JsonProperty("id") val id: String,
        @JsonProperty("service") val service: Service,
        @JsonProperty("version") var version: String,
        @JsonProperty("fileSha") var fileSha: String,
)