package gg.flyte.pluginportal.common

import java.io.File

object Constants {
    val INSTALL_DIRECTORY: File get() = PluginPortalBase.plugin.installDirectory
    val UPDATE_DIRECTORY: File get() = PluginPortalBase.plugin.updateDirectory
}