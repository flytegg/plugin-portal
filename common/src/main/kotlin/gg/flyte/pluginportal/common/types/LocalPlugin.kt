package gg.flyte.pluginportal.common.types

import java.io.File

/*
    * Represents a plugin that is installed on the local server.
 */
data class LocalPlugin(
    val id: String,
    val name: String,
    val platform: MarketplacePlatform,
    val sha256: String,
    val installedAt: Long
)