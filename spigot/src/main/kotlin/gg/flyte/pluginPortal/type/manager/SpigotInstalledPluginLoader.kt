package gg.flyte.pluginPortal.type.manager

import gg.flyte.common.api.interfaces.InstalledPluginLoader
import gg.flyte.common.type.api.plugin.InstalledPlugin
import gg.flyte.common.util.GSON
import java.io.File

class SpigotInstalledPluginLoader : InstalledPluginLoader {
    override val installedPlugins: ArrayList<InstalledPlugin>
        get() = arrayListOf()

    override fun loadInstalledPlugins(configFIle: File) {
        installedPlugins.addAll(GSON.fromJson(
            configFIle.readText(),
            ArrayList<InstalledPlugin>()::class.java
        ))
    }
}