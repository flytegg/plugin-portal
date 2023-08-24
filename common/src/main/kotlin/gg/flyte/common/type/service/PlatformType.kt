package gg.flyte.common.type.service

enum class PlatformType(vararg val supportedPlatformType: PlatformType) {
    BUKKIT,
    SPIGOT(BUKKIT),
    PAPER(BUKKIT, SPIGOT),
    FOLIA(BUKKIT, SPIGOT, PAPER),
    SPONGE,
    FORGE(SPONGE),
    NEOFORGE(FORGE),
    FABRIC,
    QUILT(FABRIC),
    DATAPACK,
    BUNGEECORD,
    WATERFALL(BUNGEECORD),
    VELOCITY,

}