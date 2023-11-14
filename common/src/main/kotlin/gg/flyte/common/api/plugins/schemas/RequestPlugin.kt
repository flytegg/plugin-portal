package gg.flyte.common.api.plugins.schemas

data class RequestPlugin(
    val id: String,
    val requestReasoning: String,
    val pluginName: String,
    val externalUrl: String,
    val username: String,
)
