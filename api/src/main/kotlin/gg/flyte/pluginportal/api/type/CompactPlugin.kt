package gg.flyte.pluginportal.api.type

data class CompactPlugin(
    val id: String,
    val name: String,
    val version: String?,
    val sha256: String?,
) { fun getUniqueName() = "$name ($id)" }