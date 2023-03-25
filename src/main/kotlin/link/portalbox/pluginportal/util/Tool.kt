package link.portalbox.pluginportal.util

import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

fun getSha(file: File): String {
    val md = MessageDigest.getInstance("SHA-256")
    FileInputStream(file).use { fis ->
        val dataBytes = ByteArray(1024)
        var nread: Int
        while (fis.read(dataBytes).also { nread = it } != -1) {
            md.update(dataBytes, 0, nread)
        }
    }
    val digest = md.digest()
    val sb = StringBuilder()
    for (b in digest) {
        sb.append(Integer.toString((b.toInt() and 0xff) + 0x100, 16).substring(1))
    }
    return sb.toString()
}

fun String.coloredComponent() = TextComponent(ChatColor.translateAlternateColorCodes('&', this))

fun getAverageColor(bi: BufferedImage): Color {
    val step = 5
    var sampled = 0
    var sumr: Long = 0
    var sumg: Long = 0
    var sumb: Long = 0
    for (x in 0 until bi.width) {
        for (y in 0 until bi.height) {
            if (x % step == 0 && y % step == 0) {
                val pixel = Color(bi.getRGB(x, y))
                sumr += pixel.red.toLong()
                sumg += pixel.green.toLong()
                sumb += pixel.blue.toLong()
                sampled++
            }
        }
    }
    return Color(
        Math.round((sumr / sampled).toFloat()),
        Math.round((sumg / sampled).toFloat()),
        Math.round((sumb / sampled).toFloat())
    )
}