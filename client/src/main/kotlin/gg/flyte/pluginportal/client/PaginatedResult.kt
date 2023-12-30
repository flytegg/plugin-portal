package gg.flyte.pluginportal.client

data class PaginatedResult<T>(
    val pagination: Pagination,
    val result: List<T>
)
