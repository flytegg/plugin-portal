package gg.flyte.pluginPortal.type.server

data class LaunchSettings(
    val flagType: FlagType,
    val noGui: Boolean,
    val autoRestart: Boolean
)
