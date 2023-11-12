package gg.flyte.common.type.api.plugin.schemas

data class Dependency(
    val name: String,
    val id: String?,
    val required: Boolean,
)