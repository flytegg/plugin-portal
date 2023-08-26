package gg.flyte.pluginPortal.type.config

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal
import gg.flyte.common.type.logger.Logger
import gg.flyte.common.util.GSON
import gg.flyte.pluginPortal.type.server.ServerManager
import java.io.File

object Config {

    lateinit var userConfig: UserConfig
    private val homeDirectory = ServerManager.getHomeFolderDirectory()
    val terminal = Terminal(
        AnsiLevel.TRUECOLOR,
        interactive = true,
        theme = Theme.Default
    )

    fun loadConfigs() {
        val file = File(homeDirectory, "settings.json")
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(GSON.toJson(UserConfig()))
        }

        userConfig = GSON.fromJson(
            File(
                ServerManager.getHomeFolderDirectory(),
                "settings.json"
            ).readText(), UserConfig::class.java
        )

        File(homeDirectory, "presets").mkdir()
        File(homeDirectory, "servers").mkdir()
        File(homeDirectory, "data").mkdir()

        Logger.enabled = userConfig.debug
    }

    fun saveConfig() {
        File(homeDirectory, "settings.json").writeText(GSON.toJson(userConfig))
    }

}