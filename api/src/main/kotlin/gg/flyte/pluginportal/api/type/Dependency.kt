package gg.flyte.pluginportal.api.type

data class Dependency(
    val name: String,
    val id: String?,
    val required: Boolean,
)