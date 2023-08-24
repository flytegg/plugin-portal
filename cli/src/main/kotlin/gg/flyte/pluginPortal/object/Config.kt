package gg.flyte.pluginPortal.`object`

import gg.flyte.pluginPortal.`object`.serializer.SerializedConfig
import gg.flyte.common.util.GSON
import gg.flyte.pluginPortal.manager.ServerManager
import java.io.File

object Config {

    lateinit var serializedConfig: SerializedConfig
    private val homeDirectory = ServerManager.getHomeFolderDirectory()

    fun loadConfigs() {
        val file = File(homeDirectory, "config.json")
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(GSON.toJson(SerializedConfig()))
        }

        this.serializedConfig = GSON.fromJson(
            File(
                ServerManager.getHomeFolderDirectory(),
                "config.json"
            ).readText(), SerializedConfig::class.java
        )

        File(homeDirectory, "presets").mkdir()
        File(homeDirectory, "servers").mkdir()
        File(homeDirectory, "data").mkdir()
    }

    fun saveConfig() {
        File(homeDirectory, "config.json").writeText(GSON.toJson(this.serializedConfig))
    }

}