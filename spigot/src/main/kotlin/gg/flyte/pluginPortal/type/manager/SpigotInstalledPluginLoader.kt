package gg.flyte.pluginPortal.type.manager

import gg.flyte.common.api.plugins.schemas.InstalledPlugin
import gg.flyte.common.api.plugins.schemas.MarketplacePlugin
import gg.flyte.common.api.plugins.schemas.toInstalledPlugin
import gg.flyte.common.util.GSON
import gg.flyte.common.util.getHashes
import gg.flyte.pluginPortal.PluginPortal
import java.io.File

object SpigotInstalledPluginLoader {
    private val configFile: File = File(PluginPortal.instance.dataFolder, "plugins.json")
    val pluginFolder: File = PluginPortal.instance.dataFolder.parentFile
    val updateFolder: File = File(pluginFolder, "update").apply { if (!exists()) mkdirs() }

    fun loadInstalledPlugins() {
        if (configFile.readText().isEmpty()) { configFile.writeText("[]") }

        GSON.fromJson(
            configFile.readText(),
            Array<InstalledPlugin>::class.java
        ).forEach { PPPluginCache.addInstalledPlugins(it) }
    }

    fun saveInstalledPlugins() {
        configFile.writeText(GSON.toJson(PPPluginCache.getInstalledPlugins()))
    }
}