package gg.flyte.pluginPortal.type.plugin

import gg.flyte.common.type.service.PlatformType
import gg.flyte.common.util.downloadFileSync
import gg.flyte.common.util.installPlugin

data class PluginInstaller(
    val id: String,
    val version: String,
    val platformType: PlatformType,
) {
    fun install() {
        downloadFileSync()
    }
}