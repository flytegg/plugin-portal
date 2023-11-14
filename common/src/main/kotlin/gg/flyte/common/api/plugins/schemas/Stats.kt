package gg.flyte.common.api.plugins.schemas

data class Stats(
    val downloads: Int,
    val ratingAverage: Long,
    val ratingCount: Int,
    val isPremium: Boolean,
    val price: Long?,
)
