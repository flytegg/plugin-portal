package gg.flyte.common

import gg.flyte.common.type.software.PaperSoftware

fun main(arrayOf: Array<String>) {
    println(PaperSoftware.getVersions())
    println(PaperSoftware.getDownloadUrl("1.20.1"))
}