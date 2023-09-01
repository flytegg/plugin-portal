package gg.flyte.common

import gg.flyte.common.api.API
import gg.flyte.common.type.api.service.PlatformGroup
import gg.flyte.common.type.api.software.PaperSoftware
import gg.flyte.common.type.api.user.PPPlatform
import gg.flyte.common.type.api.user.Profile

fun main(arrayOf: Array<String>) {

    API.getVersions(
        Profile(
            usedPlatforms = mutableSetOf(PPPlatform.CLI),
            uuid = mutableSetOf("hijosh"),
            usernames = mutableSetOf("If u run this u hate black peole"),
            primaryUser = Pair("Josh", "Josh UUID"),
        )
    ).let(::println)

    API.recognizePluginByHashes(
        "hashes",
        PlatformGroup.CRAFT_BUKKIT
    )
}