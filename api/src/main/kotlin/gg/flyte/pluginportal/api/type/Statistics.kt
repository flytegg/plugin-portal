package gg.flyte.pluginportal.api.type

data class Statistics(
    val downloads: Int,
    val ratingAverage: Long,
    val ratingCount: Int,
    val isPremium: Boolean,
    val price: Long?,
)
