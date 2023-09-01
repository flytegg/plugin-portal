package gg.flyte.pluginPortal

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import gg.flyte.common.api.API
import gg.flyte.common.type.api.user.PPPlatform
import gg.flyte.common.type.api.user.Profile
import gg.flyte.pluginPortal.commands.*
import gg.flyte.pluginPortal.commands.plugins.*
import gg.flyte.pluginPortal.commands.server.*
import gg.flyte.pluginPortal.commands.server.preset.*
import gg.flyte.pluginPortal.type.config.Config
import kotlin.concurrent.thread
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.suspendCoroutine


class PluginPortal {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Config.loadConfigs()

            API.getVersions(
                Profile(
                    usedPlatforms = mutableSetOf(PPPlatform.CLI),
                    uuid = Config.userConfig.defaultOperators.map { it.uuid }.toMutableSet(),
                    usernames = Config.userConfig.defaultOperators.map { it.name }.toMutableSet(),
                    primaryUser = Config.primaryUser
                )
            )

            PPCommand()
                .context { terminal = Terminal(ansiLevel = AnsiLevel.TRUECOLOR, interactive = true) }
                .subcommands(
                    ServerCommand()
                        .subcommands(
                            StartServerCommand(),
                            CreateServerCommand(),
                            DeleteServerCommand(),
                            ListServersCommand(),
                            SelectServerCommand(),
                            InfoServerCommand(),
                            PresetCommand()
                                .subcommands(
                                    ListPresetsCommand(),
                                    SavePresetCommand(),
                                    LoadPresetCommand(),
                                    DeletePresetCommand(),
                                ),
                        ),
                    Plugins()
                        .subcommands(
                            InstallPluginCommand(),
                            PreviewPluginCommand(),
                            UpdatePluginCommand(),
                            ListPluginsCommand(),
                            DeletePluginCommand(),
                            RequestPluginCommand(),
                            SearchPluginsCommand(),
                        ),
                    SettingsCommand()
                )
                .main(args)

        }
    }
}
