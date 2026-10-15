package gg.flyte.pluginportal.common.util

import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.types.enums.ServerType

fun currentServerTypePreference(): List<ServerType> =
    runCatching { PluginPortalBase.plugin.serverTypes }.getOrDefault(listOf(ServerType.BUKKIT))

fun currentMinecraftVersion(): String? = runCatching {
    Regex("""\b\d+\.\d+(?:\.\d+)?\b""").find(PluginPortalBase.plugin.server.minecraftVersion)?.value
}.getOrNull()
