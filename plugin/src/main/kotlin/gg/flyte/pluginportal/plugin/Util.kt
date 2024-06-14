package gg.flyte.pluginportal.plugin

import java.text.DecimalFormat

fun Int.format(): String = DecimalFormat.getIntegerInstance().format(this)