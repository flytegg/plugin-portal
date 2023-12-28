package gg.flyte.pluginportal.api.type

data class DisplayInfo(
    val name: String,
    val description: String,
    val iconURL: String,
    val extraInfo: String?,
)