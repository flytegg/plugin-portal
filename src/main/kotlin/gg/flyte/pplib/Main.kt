package gg.flyte.pplib

import gg.flyte.pplib.util.getFinalRedirect
import gg.flyte.pplib.util.isJARFileDownload

fun main() {
    println(isJARFileDownload(getFinalRedirect("https://api.spiget.org/v2/resources/9089/download")))
}