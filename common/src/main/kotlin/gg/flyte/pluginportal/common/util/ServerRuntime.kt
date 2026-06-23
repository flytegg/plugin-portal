package gg.flyte.pluginportal.common.util

import gg.flyte.pluginportal.common.types.enums.ServerType
import org.bukkit.Bukkit

fun currentServerTypePreference(): List<ServerType> {
    if (isFolia()) return listOf(ServerType.FOLIA, ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT)

    val name = runCatching { Bukkit.getName().lowercase() }.getOrDefault("")
    val version = runCatching { Bukkit.getServer().version.lowercase() }.getOrDefault("")
    val descriptor = "$name $version"

    return when {
        "purpur" in descriptor -> listOf(ServerType.PURPUR, ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT)
        "pufferfish" in descriptor -> listOf(ServerType.PUFFERFISH, ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT)
        isPaperServer(descriptor) -> listOf(ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT)
        "spigot" in descriptor -> listOf(ServerType.SPIGOT, ServerType.BUKKIT)
        else -> listOf(ServerType.BUKKIT)
    }
}

fun currentMinecraftVersion(): String? =
    normalizeMinecraftVersion(runCatching { Bukkit.getServer().bukkitVersion }.getOrNull())
        ?: normalizeMinecraftVersion(runCatching { Bukkit.getServer().version }.getOrNull())

private fun isPaperServer(descriptor: String): Boolean =
    "paper" in descriptor || runCatching {
        Class.forName("com.destroystokyo.paper.PaperConfig")
    }.isSuccess || runCatching {
        Class.forName("io.papermc.paper.configuration.Configuration")
    }.isSuccess

private val minecraftVersionPattern = Regex("""\b\d+\.\d+(?:\.\d+)?\b""")

private fun normalizeMinecraftVersion(value: String?): String? =
    value
        ?.let { minecraftVersionPattern.find(it)?.value }
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
