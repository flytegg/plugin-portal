package gg.flyte.pplib.type.error

data class PostError(
    val pluginVersion: String,
    val minecraftVersion: String,
    val stackTrace: String,
)
