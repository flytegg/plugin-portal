package gg.flyte.common.type.api.plugin

import gg.flyte.common.type.api.service.PlatformType
import gg.flyte.common.type.api.service.ServiceType

data class InstalledPlugin(
    val id: String,
    val name: String,
    val version: String,
    val primaryPlatformType: PlatformType,
    val serviceType: ServiceType,
    val sha256Hash: String,
    val downloadUrl: String,
)