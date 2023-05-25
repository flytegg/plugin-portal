package gg.flyte.pplib.type.api

data class PostError(
    val pluginVersion: String,
    val mcVersion: String,
    val stackTrace: String,
)