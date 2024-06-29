package gg.flyte.pluginportal.plugin.util

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import java.io.File
import java.text.DecimalFormat

fun Int.format(): String = DecimalFormat.getIntegerInstance().format(this)

fun File.appendLine(text: String) = appendText(text + "\n")
fun File.createIfNotExists() = apply {
    parentFile?.mkdirs()
    if (!exists()) createNewFile()
}

fun String.capitaliseFirst() = lowercase().replaceFirstChar(Char::uppercaseChar)


internal val PP_MODRINTH_ID = "5qkQnnWO"
internal val Plugin.isPluginPortal: Boolean get() = platforms[MarketplacePlatform.MODRINTH]?.id == PP_MODRINTH_ID // can add premium here too
internal val LocalPlugin.isPluginPortal: Boolean get() = platform == MarketplacePlatform.MODRINTH && platformId == PP_MODRINTH_ID