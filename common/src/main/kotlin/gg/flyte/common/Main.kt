package gg.flyte.common

import gg.flyte.common.api.API
import gg.flyte.common.api.PaperMCAPI
import gg.flyte.common.type.service.PlatformType
import gg.flyte.common.type.software.FoliaSoftware
import gg.flyte.common.util.GSON

fun main(arrayOf: Array<String>) {
    println(FoliaSoftware().getVersions())
    println(FoliaSoftware().getDownloadUrl("1.20.1"))
}