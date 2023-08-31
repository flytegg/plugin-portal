package gg.flyte.pluginPortal.type.server

import gg.flyte.common.type.service.SoftwareType
import gg.flyte.common.util.GSON
import gg.flyte.pluginPortal.type.plugin.InstalledPlugin
import gg.flyte.pluginPortal.type.server.ServerManager.getServerFolderDirectory
import java.io.File

data class ServerConfig(
    val name: String,
    val softwareType: SoftwareType,
    val version: String,
    var autoUpdatePlugins: Boolean = false,
    val launchSettings: LaunchSettings = LaunchSettings(
        FlagType.NONE,
        true,
        false
    ),
    val installedPlugins: ArrayList<InstalledPlugin> = arrayListOf()
) {
    fun getPluginsFolder(): File {
        return File(getDirectory(), "plugins").apply {
            mkdirs()
        }
    }

    fun getDirectory(): File {
        return File(getServerFolderDirectory(), name)
    }

    fun save() {
        File(getDirectory(), "config.ppm")
            .writeText(GSON.toJson(this))
    }
}

