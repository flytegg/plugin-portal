package gg.flyte.pluginPortal

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import gg.flyte.common.type.logger.Logger
import gg.flyte.pluginPortal.commands.*
import gg.flyte.pluginPortal.`object`.Config

class PluginPortal {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Config.loadConfigs()
            Logger.enabled = Config.serializedConfig.debug

            PPCommand()
                .context { terminal = Terminal(ansiLevel = AnsiLevel.TRUECOLOR, interactive = true) }
                .subcommands(
                    ServerCommand()
                        .subcommands(
                            StartServer(),
                            CreateServer(),
                            DeleteServer(),
                            ListServers(),
                            SelectServer(),
                            PresetCommand()
                                .subcommands(
                                    ListPreset(),
                                    SavePreset(),
                                    LoadPreset()),
                            ServerSettings()
                        ),
                    Plugins()
                        .subcommands(
                            InstallPlugin(),
                            PreviewPlugin(),
                            UpdatePlugin(),
                            ListPlugins(),
                            DeletePlugin(),
                            RequestPlugins(),
                            SearchPlugins(),
                        ),
                    SettingsCommand()
                )
                .main(args)

        }
    }
}
