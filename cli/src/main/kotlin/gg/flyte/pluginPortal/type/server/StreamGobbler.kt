package gg.flyte.pluginPortal.type.server

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.function.Consumer

class StreamGobbler(
    val inputStream: InputStream,
    val consumer: Consumer<String>
) : Runnable {
    override fun run() {
        BufferedReader(InputStreamReader(inputStream)).lines()
            .forEach(consumer)
    }
}