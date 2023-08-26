package gg.flyte.pluginPortal.util

import java.util.*

fun Char.toUnicode() = String.format("u+%04x", this).uppercase(Locale.getDefault())