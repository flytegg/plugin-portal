package gg.flyte.pluginportal.plugin.util

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

fun solidLine(prefix: String = "", suffix: String = "\n") = text(
    "$prefix                                                                 $suffix",
    NamedTextColor.DARK_GRAY,
    TextDecoration.STRIKETHROUGH
)