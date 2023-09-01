package gg.flyte.common.type.api.error

data class PostError(
    val pluginVersion: String,
    val minecraftVersion: String,
    val stackTrace: String,
)
