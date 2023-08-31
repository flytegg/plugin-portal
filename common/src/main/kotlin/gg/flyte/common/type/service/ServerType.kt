package gg.flyte.common.type.service

enum class ServerType(val description: String) {
    VANILLA("Strictly Vanilla"),
    PLUGINS("Supports Plugins, This Is The Most Common"),
    MODDED("Supports Mods"),
    PROXY("Supports Multiple Servers At Once"),
    SPONGE("Supports Sponge plugins and sometimes Forge mods"),
    OTHER("Other, These tend to be buggy.");


    fun getDisplayName() = "${this.name} > ${this.description}"

    fun containsSupportedSoftware(): Boolean {
        return SoftwareType.values()
            .filter { software -> software.serverType == this }
            .any { softwareType -> softwareType.softwareInterface != null }
    }
}