package gg.flyte.common.api.dataClasses.endpoints

import gg.flyte.common.type.api.plugin.InstalledPlugin
import gg.flyte.common.type.misc.HashType

data class RecognizePluginByHashesResult(
    val hashes: HashMap<Pair<HashType, String>, String>, // <Pair<HashType, Value>, PluginId>
    val plugins: ArrayList<InstalledPlugin>,
)
