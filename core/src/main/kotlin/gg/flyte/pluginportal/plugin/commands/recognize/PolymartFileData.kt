package gg.flyte.pluginportal.plugin.commands.recognize

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform

data class PolymartFileData(
    val id: String,
    val title: String,
    val version: String,
) {
    fun getNewLocalPlugin(entryId: String, sha256: String, sha512: String): LocalPlugin {
        return LocalPlugin(entryId, id, title, version, MarketplacePlatform.POLYMART, sha256, sha512, System.currentTimeMillis())
    }
}