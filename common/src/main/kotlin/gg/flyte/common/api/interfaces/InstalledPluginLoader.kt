package gg.flyte.common.api.interfaces

import gg.flyte.common.type.api.plugin.InstalledPlugin
import java.io.File

interface InstalledPluginLoader {

    abstract val configFile: File
    abstract val installedPlugins: MutableSet<InstalledPlugin>

    abstract fun addInstalledPlugin(plugin: InstalledPlugin)
    abstract fun loadInstalledPlugins()
    abstract fun saveInstalledPlugins()

}

