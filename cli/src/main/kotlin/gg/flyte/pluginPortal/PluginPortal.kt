package gg.flyte.pluginPortal

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import gg.flyte.pluginPortal.commands.*
import gg.flyte.pluginPortal.commands.plugins.RequestPluginCommand
import gg.flyte.pluginPortal.commands.plugins.SearchPluginsCommand
import gg.flyte.pluginPortal.commands.server.DeleteServerCommand
import gg.flyte.pluginPortal.commands.server.SelectServerCommand
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
                            StartServer(),
                            CreateServer(),
                            DeleteServerCommand(),
                            ListServers(),
                            SelectServerCommand(),
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
                            RequestPluginCommand(),
                            SearchPluginsCommand(),
                        ),
                    SettingsCommand()
                )
                .main(args)

        }
    }
}
