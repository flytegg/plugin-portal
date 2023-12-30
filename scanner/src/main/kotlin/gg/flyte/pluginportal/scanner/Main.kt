package gg.flyte.pluginportal.scanner

import gg.flyte.hangarwrapper.HangarClient
import gg.flyte.pluginportal.scanner.scanners.HangarScanner
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

suspend fun main() {
    HangarClient.Builder {}.build()

    while (true) {
        HangarScanner.scan()
        delay(48.hours)
    }
}