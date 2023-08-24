package gg.flyte.pluginPortal.`object`.server

data class LaunchSettings(
    val flagType: FlagType,
    val noGui: Boolean,
    val autoRestart: Boolean
)
