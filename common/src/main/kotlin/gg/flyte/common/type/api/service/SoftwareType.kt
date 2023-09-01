package gg.flyte.common.type.api.service

import gg.flyte.common.type.api.software.*
import gg.flyte.common.type.api.software.`interface`.SoftwareInterface

enum class SoftwareType(
    val serverType: ServerType,
    val primarySupportedPlatformType: PlatformType? = null,
    val description: String,
    val softwareInterface: SoftwareInterface? = null,
) {
    VANILLA(ServerType.VANILLA, description = "The plainest of the plain, no plugins, no mods, just minecraft."),

    BUKKIT(ServerType.PLUGINS, PlatformType.SPIGOT, "Old and outdated, not used.."),
    SPIGOT(ServerType.PLUGINS, PlatformType.SPIGOT, "Old and outdated, but still used."),
    PAPER(ServerType.PLUGINS, PlatformType.PAPER, "The most common, stable, and recommended.", PaperSoftware),
    PURPUR(ServerType.PLUGINS, PlatformType.PAPER, "Modified Version of Paper, with more features though less stable."),
    FOLIA(
        ServerType.PLUGINS,
        PlatformType.FOLIA, "Modified Version of Paper, This will break nearly every plugin.", FoliaSoftware
    ),

    FORGE(ServerType.MODDED, PlatformType.FORGE, "Old and outdated, but still used."),
    NEOFORGE(ServerType.MODDED, PlatformType.NEOFORGE, "New version of forge"),
    FABRIC(ServerType.MODDED, PlatformType.FABRIC, "Performance focused, great server for modded."),
    QUILT(ServerType.MODDED, PlatformType.QUILT, "Modified Version of Fabric, with more features though less stable."),

    BUNGEECORD(ServerType.PROXY, PlatformType.BUNGEECORD, "Old and outdated, but still used."),
    WATERFALL(ServerType.PROXY, PlatformType.WATERFALL, "Modified version of Bungeecord.", WaterfallSoftware),
    VELOCITY(ServerType.PROXY, PlatformType.VELOCITY, "PaperMC's remake of Bungeecord.", VelocitySoftware),
    TRAVERTINE(
        ServerType.PROXY,
        PlatformType.TRAVERTINE,
        "Modified version of Waterfall which supports 1.7.",
        TravertineSoftware
    ),

    SPONGE_VANILLA(ServerType.SPONGE, PlatformType.SPONGE, "Supports Sponge plugins"),
    SPONGE_FORGE(ServerType.SPONGE, PlatformType.SPONGE, "Supports Sponge plugins and Forge mods"),
    LANTERN(ServerType.SPONGE, PlatformType.SPONGE, "Fork of Sponge Vanilla, Supports Sponge plugins"),

    MAGMA(ServerType.OTHER, description = "Unstable, Supports plugins and mods"),
    ARCLIGHT(ServerType.OTHER, description = "Unstable, Supports plugins and mods"),
    MOHIST(ServerType.OTHER, description = "Unstable, Supports plugins and mods");


    fun getDisplayName() = "${this.name} > ${this.description}"
}