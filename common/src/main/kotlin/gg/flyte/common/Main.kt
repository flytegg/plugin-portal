package gg.flyte.common

import gg.flyte.common.api.API
import gg.flyte.common.type.service.PlatformGroup
import gg.flyte.common.type.software.PaperSoftware

fun main(arrayOf: Array<String>) {

    API.recognizePluginByHashes(
        arrayListOf(
            "31250fb61b690d526a47373dd9ac2811f7c87e74a6dc5f48ddbada1567c8d2c7",
            "54fbae4523b8a60d4a50dfc3705ff41f267f988c2c6c80cd9e2e3da3cb0aa319",
            "a36c9a0d8df3234c6eb020a977bb3ea98779c425541a23611cee4f3a36520486",
            "asdfgarsdjgljafdg",
            "f83a0676605e97bc5cda654e9d9ef42820876ba661579676e43f22af4412ae70",
            "a3443c9c7a16f10734a261dd6570c9aa50077445ba2a90c34491cd1460b5c581", // Chunky
        ).joinToString(","),
        PlatformGroup.CRAFT_BUKKIT
    ).body()!!.forEach {
        println(it.value.displayInfo.name)
    }
}