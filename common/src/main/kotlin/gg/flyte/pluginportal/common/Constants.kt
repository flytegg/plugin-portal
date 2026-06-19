package gg.flyte.pluginportal.common

import org.bukkit.Bukkit
import java.io.File

object Constants {
    val INSTALL_DIRECTORY: File get() = PluginPortalBase.plugin.dataFolder.parentFile
    val UPDATE_DIRECTORY: File get() = Bukkit.getUpdateFolderFile()
}