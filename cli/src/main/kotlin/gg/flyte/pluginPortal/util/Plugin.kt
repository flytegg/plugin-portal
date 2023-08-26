package gg.flyte.pluginPortal.util

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptInput
import gg.flyte.common.api.API
import gg.flyte.common.api.dataClasses.endpoints.PaginatedResultMarketplacePlugin
import gg.flyte.common.api.dataClasses.pagination.Pagination


fun KInquirer.promptPluginSearch(name: String): PaginatedResultMarketplacePlugin {

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

    return PaginatedResultMarketplacePlugin(Pagination(0, 0, 0), listOf())
}