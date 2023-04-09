package link.portalbox.pluginportal.util

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


//fun getAverageColor(bi: BufferedImage): Color {
//    val step = 5
//    var sampled = 0
//    var sumr: Long = 0
//    var sumg: Long = 0
//    var sumb: Long = 0
//    for (x in 0 until bi.width) {
//        for (y in 0 until bi.height) {
//            if (x % step == 0 && y % step == 0) {
//                val pixel = Color(bi.getRGB(x, y))
//                sumr += pixel.red.toLong()
//                sumg += pixel.green.toLong()
//                sumb += pixel.blue.toLong()
//                sampled++
//            }
//        }
//    }
//    return Color(
//        Math.round((sumr / sampled).toFloat()),
//        Math.round((sumg / sampled).toFloat()),
//        Math.round((sumb / sampled).toFloat())
//    )
//}

data class GameVersion(val major: Int, val minor: Int, val patch: Int) {
    operator fun compareTo(other: GameVersion): Int {
        if (major != other.major) return major - other.major
        if (minor != other.minor) return minor - other.minor
        return patch - other.patch
    }
}

fun getVersion(versionString: String): GameVersion {
    val parts = Regex("(\\d+)\\.(\\d+)\\.(\\d+)").find(versionString) ?: return GameVersion(0, 0, 0)

    val major = parts.groupValues[1].toIntOrNull() ?: 0
    val minor = parts.groupValues[2].toIntOrNull() ?: 0
    val patch = parts.groupValues[3].toIntOrNull() ?: 0

    return GameVersion(major, minor, patch)
}

fun getVersionRange(versions: List<String>): String {
    assert(versions.isNotEmpty())
    val sortedVersions = versions.sortedWith { a, b -> getVersion(a).compareTo(getVersion(b)) }

    val oldest = getVersion(sortedVersions.first()).run { "${major}.${minor}" }
    val latest = getVersion(sortedVersions.last()).run { "${major}.${minor}" }

    if (oldest == latest) return oldest
    return "$oldest-$latest"
}

inline fun <T> T.applyIf(shouldApply: Boolean, block: T.() -> Unit): T = apply {
    if (shouldApply) {
        block(this)
    }
}