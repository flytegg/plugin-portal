package gg.flyte.common.type.plugin

import gg.flyte.common.type.service.PlatformType
import gg.flyte.common.type.service.ServiceType

data class InstalledPlugin(
    val id: String,
    val name: String,
    val version: String,
    val primaryPlatformType: PlatformType,
    val serviceType: ServiceType,
    val sha256Hash: String,
)