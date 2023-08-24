package gg.flyte.pluginPortal.`object`.serializer

import gg.flyte.common.type.service.PlatformType
import gg.flyte.common.type.service.SoftwareType
import gg.flyte.common.util.GSON
import gg.flyte.pluginPortal.manager.ServerManager.getServerFolderDirectory
import gg.flyte.pluginPortal.`object`.server.FlagType
import gg.flyte.pluginPortal.`object`.server.LaunchSettings
import gg.flyte.pluginPortal.`object`.server.ServerVersion
import java.io.File

data class SerializedServer(
    val name: String,
    val softwareType: SoftwareType,
    val version: ServerVersion = ServerVersion.entries.last(),
    var autoUpdatePlugins: Boolean = false,
    val launchSettings: LaunchSettings = LaunchSettings(
        FlagType.NONE,
        true,
        false
    ),
) {
    fun getPluginsFolder(): File {
        return File(getDirectory(), "plugins").apply {
            createNewFile()
        }
    }

    fun getDirectory(): File {
        return File(getServerFolderDirectory(), name)
    }
}

