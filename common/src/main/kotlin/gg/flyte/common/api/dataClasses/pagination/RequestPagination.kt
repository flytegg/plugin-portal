package gg.flyte.common.api.dataClasses.pagination

data class RequestPagination(
    var limit: Int = 50,
    var offset: Int = 0,
)
