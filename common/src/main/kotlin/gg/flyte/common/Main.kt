package gg.flyte.common

import gg.flyte.common.api.API
import gg.flyte.common.type.service.PlatformType
import gg.flyte.common.util.GSON

fun main() {
    //println(API.requestPluginById("EssentialsX:Essentials", PlatformType.PAPER).body())
    println(
        GSON.toJson(
            API.searchForPluginsByName(
                "E",
                PlatformType.PAPER.name,
                4,
                25
            ).body()
        )
    )

}
