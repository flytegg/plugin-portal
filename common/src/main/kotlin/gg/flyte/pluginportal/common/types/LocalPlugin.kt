package gg.flyte.pluginportal.common.types

/*
    * Represents a plugin that is installed on the local server.
 */
data class LocalPlugin(
    val id: String,
    val name: String,
    val platform: MarketplacePlatform,
    val installedAt: Long
)