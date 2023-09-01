package gg.flyte.common.type.api.plugin

import gg.flyte.common.type.api.service.ServiceType

data class RequestPlugin(
    val id: String,
    val service: ServiceType,
    val requestReasoning: String,
    val pluginName: String,
    val externalUrl: String,
    val username: String,
)
