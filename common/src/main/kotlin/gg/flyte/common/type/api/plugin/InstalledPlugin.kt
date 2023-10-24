package gg.flyte.common.type.api.plugin

import gg.flyte.common.type.api.service.ServiceType
import gg.flyte.common.type.misc.HashType

data class InstalledPlugin(
    val id: String,
    val name: String,
    val version: String,
    val hashes: HashMap<HashType, String>,
    val downloadUrl: String,
)