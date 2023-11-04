package gg.flyte.common.api.dataClasses

data class Dependency(
    val name: String,
    val id: String?,
    val required: Boolean,
)