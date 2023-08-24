package gg.flyte.pplib.type.plugin

data class RequestPlugin(
    val id: String,
    val service: String,
    val requestReasoning: String,
    val pluginName: String,
    val externalUrl: String,
    val username: String,
)
