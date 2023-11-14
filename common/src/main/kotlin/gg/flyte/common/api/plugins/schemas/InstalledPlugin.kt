package gg.flyte.common.api.plugins.schemas

data class InstalledPlugin(
    val id: String,
    val name: String,
    val version: String?,
    val hashes: HashMap<HashType, String>?,
)

fun MarketplacePlugin.toInstalledPlugin() = InstalledPlugin(
    this.id,
    this.displayInfo.name,
    null,
    null,
)