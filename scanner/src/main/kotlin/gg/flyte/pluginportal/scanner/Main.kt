package gg.flyte.pluginportal.scanner

import gg.flyte.hangarwrapper.HangarClient
import gg.flyte.pluginportal.scanner.scanners.HangarScanner

suspend fun main() {
    HangarClient.Builder {

    }.build()
    HangarScanner.scan()
}