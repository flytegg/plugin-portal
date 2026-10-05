package gg.flyte.pluginportal.common.types.enums

import com.google.gson.annotations.SerializedName

enum class ServerPlatform {
    VANILLA,
    BUKKIT,
    PAPER,
    PROXY,
    FOLIA,
    MODDED
}

enum class ServerType(val platform: ServerPlatform) {
    @SerializedName("folia")
    FOLIA(ServerPlatform.FOLIA),
    @SerializedName("bukkit")
    BUKKIT(ServerPlatform.BUKKIT),
    @SerializedName("spigot")
    SPIGOT(ServerPlatform.BUKKIT),
    @SerializedName("paper")
    PAPER(ServerPlatform.PAPER),
    @SerializedName("pufferfish")
    PUFFERFISH(ServerPlatform.PAPER),
    @SerializedName("purpur")
    PURPUR(ServerPlatform.PAPER),
    @SerializedName("bungeecord")
    BUNGEECORD(ServerPlatform.PROXY),
    @SerializedName("waterfall")
    WATERFALL(ServerPlatform.PROXY),
    @SerializedName("velocity")
    VELOCITY(ServerPlatform.PROXY),
    @SerializedName("forge")
    FORGE(ServerPlatform.MODDED),
    @SerializedName("neoforge")
    NEOFORGE(ServerPlatform.MODDED),
    @SerializedName("fabric")
    FABRIC(ServerPlatform.MODDED),
    @SerializedName("quilt")
    QUILT(ServerPlatform.MODDED),
    @SerializedName("sponge")
    SPONGE(ServerPlatform.MODDED),
    @SerializedName("datapack")
    DATAPACK(ServerPlatform.VANILLA),
}