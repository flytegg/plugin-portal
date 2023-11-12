package gg.flyte.common.type.api.plugin

import gg.flyte.common.type.api.plugin.schemas.MarketplacePlugin
import gg.flyte.common.type.misc.HashType
import java.util.EnumMap

data class InstalledPlugin(
    val id: String,
    val name: String,
    val version: String?,
    val hashes: HashMap<HashType, String>?,
    val downloadUrl: String?,
)

fun MarketplacePlugin.toInstalledPlugin() = InstalledPlugin(
    this.id,
    this.displayInfo.name,
    null,
    null,
    null,
)