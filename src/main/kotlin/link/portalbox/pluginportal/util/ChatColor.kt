package link.portalbox.pluginportal.util

import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor

object ChatColor {

    fun String?.color(): String = this?.let { ChatColor.translateAlternateColorCodes('&', it) } ?: ""

    fun String?.colorOutput(): String = this?.let { ChatColor.translateAlternateColorCodes('&', "&7&l[&b&lPP&7&l] &8&l> $it") }
            ?: ""


    fun String.coloredComponent() = TextComponent(ChatColor.translateAlternateColorCodes('&', this))


}