package gg.flyte.common.api.dataClasses.pagination

data class Pagination(
    var limit: Int = 50,
    var offset: Int = 0,
    var total: Int = 0, // Leave 0 when searching, this will only be useful when receiving data.
)
