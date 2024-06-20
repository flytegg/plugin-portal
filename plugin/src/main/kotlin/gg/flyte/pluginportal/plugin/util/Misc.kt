package gg.flyte.pluginportal.plugin.util

import java.io.File
import java.text.DecimalFormat

fun Int.format(): String = DecimalFormat.getIntegerInstance().format(this)

fun File.appendLine(text: String) = appendText(text + "\n")
fun File.createIfNotExists() = apply {
    parentFile?.mkdirs()
    if (!exists()) createNewFile()
}

fun String.capitaliseFirst() = lowercase().replaceFirstChar(Char::uppercaseChar)