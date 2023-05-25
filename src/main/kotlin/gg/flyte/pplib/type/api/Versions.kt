package gg.flyte.pplib.type.api

import com.fasterxml.jackson.annotation.JsonProperty

data class Versions(
    @JsonProperty("versions")
    val versions: Map<String, String>
)
