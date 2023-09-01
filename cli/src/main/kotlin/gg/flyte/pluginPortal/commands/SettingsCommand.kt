package gg.flyte.pluginPortal.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.table.table
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptConfirm
import gg.flyte.common.util.GSON
import gg.flyte.common.util.addDashesToStringUUID
import gg.flyte.common.util.getStringFromURL
import gg.flyte.common.util.toJson
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.config.Modifier
import gg.flyte.pluginPortal.type.config.UserSetting
import gg.flyte.pluginPortal.type.server.ServerOperator
import gg.flyte.pluginPortal.util.promptBetterInput
import gg.flyte.pluginPortal.util.promptBetterList
import java.util.*

class SettingsCommand : CliktCommand(
    name = "settings",
    help = "Change PluginPortal settings"
) {
    override fun run() {
        val settingDisplayName: String = KInquirer.promptBetterList(
            message = "Select a setting to change",
            choices = UserSetting.entries.map { "${it.displayName} > ${it.description}" }
        )

        val setting = UserSetting.entries.firstOrNull { it.displayName == settingDisplayName.substringBefore(" >") }!!

        when (setting.type) {
            Boolean::class.java -> {
                val value: Boolean = KInquirer.promptConfirm(
                    message = setting.displayName,
                    default = true // current value from config
                )

                Config.userConfig = GSON.toJsonTree(Config.userConfig).asJsonObject.apply {
                    addProperty(setting.variableName, value)
                }.let {
                    GSON.fromJson(it, Config.userConfig::class.java)
                }
            }

            List::class.java -> {
                val value: Modifier = KInquirer.promptBetterList(
                    message = setting.displayName,
                    choices = Modifier.entries.map { it.name }
                ).let { Modifier.valueOf(it) }

                when (value) {
                    Modifier.ADD -> {
                        Config.userConfig.defaultOperators.add(
                            KInquirer.promptBetterInput(
                                message = "Enter a player name to add to the default operators list:"
                            ).let { name ->
                                ServerOperator(
                                    getStringFromURL("https://api.mojang.com/users/profiles/minecraft/$name").let { uuid ->
                                        if (uuid.contains("Couldn't find any profile with name", true)) {
                                            Config.terminal.println(table {
                                                body { row("$name Not Found") }
                                            })
                                            return
                                        } else {
                                            uuid.substring(12, 44).addDashesToStringUUID()
                                        }
                                    },
                                    name
                                )
                            }
                        )
                    }

                    Modifier.REMOVE -> {
                        if (Config.userConfig.defaultOperators.isEmpty()) {
                            echo("There are no players in the default operators list")
                            return
                        }

                        Config.userConfig.defaultOperators.remove(
                            Config.userConfig.defaultOperators.find {
                                it.name == KInquirer.promptBetterList(
                                    message = "Select a player name to remove from the default operators list:",
                                    choices = Config.userConfig.defaultOperators.map { op -> op.name }
                                )
                            }
                        )
                    }
                }

            }

            else -> {
                echo("Unsupported setting type: ${setting.type}")
            }
        }

        Config.saveConfig()
    }
}