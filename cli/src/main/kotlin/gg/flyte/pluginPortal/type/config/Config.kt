package gg.flyte.pluginPortal.type.config

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import com.github.kinquirer.KInquirer
import gg.flyte.common.type.logger.Logger
import gg.flyte.common.util.GSON
import gg.flyte.common.util.addDashesToStringUUID
import gg.flyte.common.util.getStringFromURL
import gg.flyte.pluginPortal.type.server.ServerManager
import gg.flyte.pluginPortal.util.promptBetterInput
import java.io.File
import java.util.UUID

object Config {

    lateinit var userConfig: UserConfig
    private val homeDirectory = ServerManager.getHomeFolderDirectory()
    val terminal = Terminal(
        AnsiLevel.TRUECOLOR,
        interactive = true,
        theme = Theme.Default
    )

    var primaryUser: Pair<String, String>? = null

    fun loadConfigs() {
        val file = File(homeDirectory, "settings.json")
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(GSON.toJson(UserConfig()))

            primaryUser = promptUsernameInput()
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


    private fun promptUsernameInput(): Pair<String, String> {
        KInquirer.promptBetterInput(
            message = "Please enter your Minecraft username:",
            validation = { it.isNotEmpty() }
        ).let { name ->
            getStringFromURL("https://api.mojang.com/users/profiles/minecraft/$name").let { uuid ->
                if (uuid.contains("Couldn't find any profile with name", true)) {
                    terminal.println(table {
                        body { row("$name Not Found") }
                    })
                } else {
                    return Pair(name, uuid.substring(12, 44).addDashesToStringUUID())
                }
            }
        }

        return promptUsernameInput()
    }

}