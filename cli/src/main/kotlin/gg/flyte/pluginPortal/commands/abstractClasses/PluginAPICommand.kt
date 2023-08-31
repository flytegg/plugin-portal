package gg.flyte.pluginPortal.commands.abstractClasses

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.kinquirer.KInquirer
import gg.flyte.common.api.API
import gg.flyte.common.api.dataClasses.MarketplacePlugin
import gg.flyte.pluginPortal.util.promptBetterList

abstract class PluginAPICommand(
    name: String,
    help: String
) : CliktCommand(
    name = name,
    help = help
) {
    val name: String by option()
        .prompt("Enter Plugin Name")
        .help("The name of the plugin to search for.")

    lateinit var plugin: MarketplacePlugin

    override fun run() {
        val plugins = API.searchForPluginsByName(
            name,
            "PAPER",
            25,
            0
        )

        val pluginName = KInquirer.promptBetterList(
            "Found ${plugins.body()?.pagination?.total} plugins. Select a plugin to find out more, more specific search terms may be needed to find a plugin.",
            plugins.body()?.result?.map { "${it.displayInfo.name} - ${it.displayInfo.description}" }?.apply {
                toMutableList()
            } ?: listOf("Exit"),
            pageSize = 7,
        )

        if (pluginName.isEmpty() || pluginName == "Exit") return

        plugin = plugins.body()?.result
            ?.find { "${it.displayInfo.name} - ${it.displayInfo.description}" == pluginName } ?: return

        finishCommand(plugin)
    }

    abstract fun finishCommand(plugin: MarketplacePlugin)
}