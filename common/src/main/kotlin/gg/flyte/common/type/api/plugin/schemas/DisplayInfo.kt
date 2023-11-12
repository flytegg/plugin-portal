package gg.flyte.common.type.api.plugin.schemas

data class DisplayInfo(
    val name: String,
    val description: String,
    val iconURL: String,
    val extraInfo: String?,
)