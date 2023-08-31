package gg.flyte.pluginPortal.type.plugin

import gg.flyte.common.type.service.PlatformType

data class InstalledPlugin(
    val id: String,
    val version: String,
    val platformType: PlatformType,
)