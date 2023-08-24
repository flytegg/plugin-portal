package gg.flyte.common.type.plugin

import gg.flyte.common.type.service.ServiceType

data class RequestPlugin(
    val id: String,
    val service: ServiceType,
    val requestReasoning: String,
    val pluginName: String,
    val externalUrl: String,
    val username: String,
)
