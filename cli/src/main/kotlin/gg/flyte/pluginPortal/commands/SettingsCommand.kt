package gg.flyte.pluginPortal.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptConfirm
import com.github.kinquirer.components.promptInput
import com.github.kinquirer.components.promptList
import gg.flyte.common.util.GSON
import gg.flyte.pluginPortal.`object`.Config
import gg.flyte.pluginPortal.`object`.Modifier
import gg.flyte.pluginPortal.`object`.UserSetting

class SettingsCommand : CliktCommand(
    name = "settings",
    help = "Change PluginPortal settings"
) {
    override fun run() {
        val settingDisplayName: String = KInquirer.promptList(
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

                Config.serializedConfig = GSON.toJsonTree(Config.serializedConfig).asJsonObject.apply {
                    addProperty(setting.variableName, value)
                }.let {
                    GSON.fromJson(it, Config.serializedConfig::class.java)
                }
            }

            List::class.java -> {
                val value: Modifier = KInquirer.promptList(
                    message = setting.displayName,
                    choices = Modifier.entries.map { it.name }
                ).let { Modifier.valueOf(it) }

                when (value) {
                    Modifier.ADD -> {
                        Config.serializedConfig.defaultOperators.add(
                            KInquirer.promptInput(
                                message = "Enter a player name to add to the default operators list:"
                            )
                        )
                    }
                    Modifier.REMOVE -> {
                        if (Config.serializedConfig.defaultOperators.isEmpty()) {
                            echo("There are no players in the default operators list")
                            return
                        }

                        Config.serializedConfig.defaultOperators.remove(
                            KInquirer.promptList(
                                message = "Select a player name to remove from the default operators list:",
                                choices = Config.serializedConfig.defaultOperators
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