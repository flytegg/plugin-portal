package gg.flyte.common.api.dataClasses.endpoints

import gg.flyte.common.api.dataClasses.MarketplacePlugin
import gg.flyte.common.api.dataClasses.pagination.Pagination

data class PaginatedResultMarketplacePlugin (
    val pagination: Pagination,
    val result: List<MarketplacePlugin>
)