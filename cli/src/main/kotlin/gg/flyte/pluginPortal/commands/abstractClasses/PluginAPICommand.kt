package gg.flyte.pluginPortal.commands.abstractClasses

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.mordant.table.table
import com.github.kinquirer.KInquirer
import gg.flyte.common.api.API
import gg.flyte.common.api.dataClasses.MarketplacePlugin
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.server.ServerManager
import gg.flyte.pluginPortal.util.promptBetterInput
import gg.flyte.pluginPortal.util.promptBetterList

abstract class PluginAPICommand(
    name: String,
    help: String
) : CliktCommand(
    name = name,
    help = help
) {
    lateinit var plugin: MarketplacePlugin

    override fun run() {
        if (ServerManager.noServerFoundCheck()) return

        val name = KInquirer.promptBetterInput("Enter Plugin Name:")

        val plugins = API.searchForPluginsByName(
            name,
            "PAPER",
            25,
            0
        )

        if (plugins.body()?.result.isNullOrEmpty()) {
            Config.terminal.println(table {
                header { row("No plugins found!") }
                body { row("Try a different search term.") }
            })

            return
        }

        val pluginName = KInquirer.promptBetterList(
            "Found ${plugins.body()?.pagination?.total} plugins. Select a plugin to find out more, more specific search terms may be needed to find a plugin.",
            plugins.body()?.result
                ?.map { "${it.displayInfo.name} - ${it.displayInfo.description}" }!!,
            pageSize = 7,
        )

        if (pluginName.isEmpty() || pluginName == "Exit") return

        plugin = plugins.body()?.result
            ?.find { "${it.displayInfo.name} - ${it.displayInfo.description}" == pluginName } ?: return

        finishCommand(plugin)
    }

    abstract fun finishCommand(plugin: MarketplacePlugin)
}