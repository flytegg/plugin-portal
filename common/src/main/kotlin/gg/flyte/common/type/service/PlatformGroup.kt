package gg.flyte.common.type.service

enum class PlatformGroup {
    CRAFT_BUKKIT,
    SPONGE_VANILLA,
    SPONGE_FORGE,
    FORGE,
    FABRIC,
    BUNGEECORD,
    WATERFALL,
    TRAVERTINE,
    VELOCITY,
    DATAPACK,
}


fun findPlatformTypesFromGroup(platformGroup: PlatformGroup): List<PlatformType> {
    return PlatformType.entries.filter { platformGroup in it.platformGroup }
}