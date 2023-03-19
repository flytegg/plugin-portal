package link.portalbox.pluginportal.util

import org.bukkit.ChatColor

object ChatColor {

    fun String?.color(): String = this?.let { ChatColor.translateAlternateColorCodes('&', it) } ?: ""

}