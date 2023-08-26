package gg.flyte.pluginPortal.type.server

import gg.flyte.common.type.service.SoftwareType
import gg.flyte.pluginPortal.type.server.ServerManager.getServerFolderDirectory
import java.io.File

data class ServerConfig(
    val name: String,
    val softwareType: SoftwareType,
    val version: ServerVersion = ServerVersion.entries.last(),
    var autoUpdatePlugins: Boolean = false,
    val launchSettings: LaunchSettings = LaunchSettings(
        FlagType.NONE,
        true,
        false
    ),
    val installedPlugins: HashSet<String> = hashSetOf() // Plugin id's
) {
    fun getPluginsFolder(): File {
        return File(getDirectory(), "plugins").apply {
            mkdirs()
        }
    }

    fun getDirectory(): File {
        return File(getServerFolderDirectory(), name)
    }
}

