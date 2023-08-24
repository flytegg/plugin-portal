package gg.flyte.common.type.service

enum class SoftwareType(val serverType: ServerType, val primarySupportedPlatformType: PlatformType? = null, val description: String, val shouldHideDownload: Boolean = false) {
    VANILLA(ServerType.VANILLA, description = "The plainest of the plain, no plugins, no mods, just minecraft."),

    BUKKIT(ServerType.BUKKIT, PlatformType.SPIGOT, "Old and outdated, not used..",true),
    SPIGOT(ServerType.BUKKIT, PlatformType.SPIGOT, "Old and outdated, but still used."),
    PAPER(ServerType.BUKKIT, PlatformType.PAPER, "The most common, stable, and recommended."),
    PURPUR(ServerType.BUKKIT, PlatformType.PAPER, "Modified Version of Paper, with more features though less stable."),
    FOLIA(ServerType.BUKKIT, PlatformType.FOLIA, "Modified Version of Paper, This will break nearly every plugin."),

    FORGE(ServerType.MODDED, PlatformType.FORGE, "Old and outdated, but still used."),
    NEOFORGE(ServerType.MODDED, PlatformType.NEOFORGE, "New version of forge"),
    FABRIC(ServerType.MODDED, PlatformType.FABRIC, "Performance focused, great server for modded."),
    QUILT(ServerType.MODDED, PlatformType.QUILT, "Modified Version of Fabric, with more features though less stable."),

    BUNGEECORD(ServerType.PROXY, PlatformType.BUNGEECORD,"Old and outdated, but still used."),
    WATERFALL(ServerType.PROXY, PlatformType.WATERFALL,"Modified version of Bungeecord."),
    VELOCITY(ServerType.PROXY, PlatformType.VELOCITY, "PaperMC's remake of Bungeecord."),

    SPONGE_VANILLA(ServerType.SPONGE, PlatformType.SPONGE,  "Supports Sponge plugins"),
    SPONGE_FORGE(ServerType.SPONGE, PlatformType.SPONGE, "Supports Sponge plugins and Forge mods"),
    LANTERN(ServerType.SPONGE, PlatformType.SPONGE, "Fork of Sponge Vanilla, Supports Sponge plugins");

//    MAGMA(ServerType.OTHER, "Unstable, Supports plugins and mods"),
//    ARCLIGHT(ServerType.OTHER, "Unstable, Supports plugins and mods"),
//    MOHIST(ServerType.OTHER, "Unstable, Supports plugins and mods");


    fun getDisplayName() = "${this.name} > ${this.description}"
}