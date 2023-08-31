package gg.flyte.pluginPortal.type.plugin

import gg.flyte.common.type.service.PlatformType
import gg.flyte.common.type.service.ServiceType

data class InstalledPlugin(
    val id: String,
    val name: String,
    val version: String,
    val platformType: PlatformType,
    val serviceType: ServiceType,
    val sha256Hash: String,
)