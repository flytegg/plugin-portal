package gg.flyte.pluginportal.client

data class Pagination(
    val limit: Int,
    val offset: Int,
    val count: Int,
)