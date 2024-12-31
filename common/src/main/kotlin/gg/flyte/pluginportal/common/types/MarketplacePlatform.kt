package gg.flyte.pluginportal.common.types

enum class MarketplacePlatform {
    MODRINTH,
    HANGAR,
    SPIGOTMC,
    ;

    companion object {
        fun of(name: String): MarketplacePlatform? = runCatching { valueOf(name.uppercase()) }.getOrNull()
    }
}