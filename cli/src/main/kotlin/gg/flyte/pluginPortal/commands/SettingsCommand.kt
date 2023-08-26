package gg.flyte.pluginPortal.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptConfirm
import com.github.kinquirer.components.promptInput
import gg.flyte.common.util.GSON
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.config.Modifier
import gg.flyte.pluginPortal.type.config.UserSetting
import gg.flyte.pluginPortal.util.promptBetterList

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
                            KInquirer.promptInput(
                                message = "Enter a player name to add to the default operators list:"
                            )
                        )
                    }
                    Modifier.REMOVE -> {
                        if (Config.userConfig.defaultOperators.isEmpty()) {
                            echo("There are no players in the default operators list")
                            return
                        }

                        Config.userConfig.defaultOperators.remove(
                            KInquirer.promptBetterList(
                                message = "Select a player name to remove from the default operators list:",
                                choices = Config.userConfig.defaultOperators
                            )
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