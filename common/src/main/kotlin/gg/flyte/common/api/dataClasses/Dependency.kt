package gg.flyte.common.api.dataClasses

import gg.flyte.common.type.service.PlatformType

data class Dependency(
    val name: String,
    val id: String?,
    val platformType: PlatformType,
    val required: Boolean, // SoftDepend == false, Depend == true
)
