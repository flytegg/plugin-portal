package gg.flyte.pluginPortal.type.server

data class ServerOperator(
    val uuid: String,
    val name: String,
    val level: Int = 4,
    val bypassesPlayerLimit: Boolean = false,
)
