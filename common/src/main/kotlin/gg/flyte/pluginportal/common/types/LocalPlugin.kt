package gg.flyte.pluginportal.common.types

/*
    * Represents a plugin that is installed on the local server.
 */
data class LocalPlugin(
    val platformId: String,
    val name: String,
    val version: String,
    val platform: MarketplacePlatform,
    val sha256: String,
    val sha512: String,
    val installedAt: Long
)