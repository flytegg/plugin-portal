package gg.flyte.pluginPortal

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import gg.flyte.pluginPortal.commands.*
import gg.flyte.pluginPortal.commands.plugins.*
import gg.flyte.pluginPortal.commands.server.*
import gg.flyte.pluginPortal.commands.server.preset.SavePresetCommand
import gg.flyte.pluginPortal.type.config.Config


class PluginPortal {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Config.loadConfigs()

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
                            PresetCommand()
                                .subcommands(
                                    ListPreset(),
                                    SavePresetCommand(),
                                    LoadPreset()),
                            ServerSettings()
                        ),
                    Plugins()
                        .subcommands(
                            InstallPluginCommand(),
                            PreviewPluginCommand(),
                            UpdatePlugin(),
                            ListPluginsCommand(),
                            DeletePlugin(),
                            RequestPluginCommand(),
                            SearchPluginsCommand(),
                        ),
                    SettingsCommand()
                )
                .main(args)

        }
    }
}
