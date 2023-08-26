package gg.flyte.common.type.service

enum class PlatformType(vararg val platformGroup: PlatformGroup) {
    BUKKIT(PlatformGroup.CRAFT_BUKKIT),
    SPIGOT(PlatformGroup.CRAFT_BUKKIT),
    PAPER(PlatformGroup.CRAFT_BUKKIT),
    FOLIA(PlatformGroup.CRAFT_BUKKIT),
    SPONGE(PlatformGroup.SPONGE_VANILLA, PlatformGroup.SPONGE_FORGE),
    FORGE(PlatformGroup.FORGE, PlatformGroup.SPONGE_FORGE),
    NEOFORGE(PlatformGroup.FORGE),
    FABRIC(PlatformGroup.FABRIC),
    QUILT(PlatformGroup.FABRIC),
    DATAPACK(PlatformGroup.DATAPACK),
    BUNGEECORD(PlatformGroup.BUNGEECORD),
    WATERFALL(PlatformGroup.FABRIC),
    VELOCITY(PlatformGroup.VELOCITY),
}