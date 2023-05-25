package gg.flyte.pplib.type.api

import gg.flyte.pplib.type.Service

data class RequestPlugin(
    val id: String,
    val service: Service,
    val requestReasoning: String,
    val pluginName: String,
    val externalUrl: String,
    val username: String,
)