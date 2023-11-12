package gg.flyte.common.type.api.plugin.schemas

data class Stats(
    val downloads: Int,
    val ratingAverage: Long,
    val ratingCount: Int,
    val isPremium: Boolean,
    val price: Long?,
)
