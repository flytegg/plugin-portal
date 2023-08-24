package gg.flyte.common.type.error

data class PostError(
    val pluginVersion: String,
    val minecraftVersion: String,
    val stackTrace: String,
)
