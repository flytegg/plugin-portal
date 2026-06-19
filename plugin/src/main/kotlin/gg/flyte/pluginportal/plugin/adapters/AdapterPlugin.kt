package gg.flyte.pluginportal.plugin.adapters

data class AdapterPlugin(
    val platformId: String,
    val name: String,
    val version: String,
    val platform: AdapterPlatform,
    val sha256: String,
    val sha512: String,
    val installedAt: Long
)
