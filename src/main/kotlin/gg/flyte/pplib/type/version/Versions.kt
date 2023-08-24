package gg.flyte.pplib.type.version

import com.fasterxml.jackson.annotation.JsonProperty

data class Versions(
    @JsonProperty("versions") val versions: Map<String, String>
)