package gg.flyte.pluginportal.api.type

data class CompactPlugin(
    val id: String,
    val name: String,
    val version: String?,
) { fun getUniqueName() = "$name ($id)" }