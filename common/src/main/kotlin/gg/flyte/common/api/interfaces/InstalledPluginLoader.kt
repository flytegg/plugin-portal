package gg.flyte.common.api.interfaces

import gg.flyte.common.type.api.plugin.InstalledPlugin
import java.io.File

interface InstalledPluginLoader {

    abstract val installedPlugins: ArrayList<InstalledPlugin>
    abstract fun loadInstalledPlugins(configFIle: File)

}