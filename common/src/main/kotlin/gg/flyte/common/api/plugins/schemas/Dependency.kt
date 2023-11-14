package gg.flyte.common.api.plugins.schemas

data class Dependency(
    val name: String,
    val id: String?,
    val required: Boolean,
)