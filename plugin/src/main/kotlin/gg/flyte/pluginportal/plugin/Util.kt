package gg.flyte.pluginportal.plugin

import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.command.CommandSender
import java.io.File
import java.text.DecimalFormat

private val pluginAudience = BukkitAudiences.create(PluginPortal.instance)

fun Int.format(): String = DecimalFormat.getIntegerInstance().format(this)

fun File.appendLine(text: String) = appendText(text + "\n")
fun File.createIfNotExists() = apply {
    parentFile.mkdirs()
    if (!exists()) createNewFile()
}

fun CommandSender.asAudience() = pluginAudience.sender(this)