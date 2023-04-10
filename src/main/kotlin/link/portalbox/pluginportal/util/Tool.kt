package link.portalbox.pluginportal.util

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.file.GameVersion
import link.portalbox.pplib.util.getLatestPPVersion
import link.portalbox.pplib.util.getPPVersions
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.regex.Matcher
import java.util.regex.Pattern

fun getSHA(file: File): String {
    if (file.isDirectory) return ""

    val messageDigest = MessageDigest.getInstance("SHA-256")

    FileInputStream(file).use { fileInputStream ->
        val dataBytes = ByteArray(1024)
        var nread: Int
        while (fileInputStream.read(dataBytes).also { nread = it } != -1) {
            messageDigest.update(dataBytes, 0, nread)
        }
    }

    return messageDigest.digest().joinToString("") { "%02x".format(it) }
}

fun getVersion(versionString: String): GameVersion {
    val parts = Regex("(\\d+)\\.(\\d+)\\.(\\d+)").find(versionString) ?: return GameVersion(0, 0, 0)

    val major = parts.groupValues[1].toIntOrNull() ?: 0
    val minor = parts.groupValues[2].toIntOrNull() ?: 0
    val patch = parts.groupValues[3].toIntOrNull() ?: 0

    return GameVersion(major, minor, patch)
}

inline fun <T> T.applyIf(shouldApply: Boolean, block: T.() -> Unit): T = apply {
    if (shouldApply) {
        block(this)
    }
}

fun isLatestVersion(pluginPortal: PluginPortal): Boolean {
    return getLatestPPVersion() == pluginPortal.description.version
}

fun deleteOutdatedPP(pluginPortal: PluginPortal) {
    var sha256 = getPPVersions()?.values
    for (file in pluginPortal.dataFolder.parentFile.listFiles() ?: return) {
        runCatching {
            if (getSHA(file) in sha256!!) {
                println("Deleting ${file.name}...")
                file.delete()
            }
        }
    }
}
