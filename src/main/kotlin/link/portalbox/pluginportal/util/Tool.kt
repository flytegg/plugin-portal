package link.portalbox.pluginportal.util

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.file.GameVersion
import link.portalbox.pplib.util.getLatestPPVersion
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

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
    var latestVersion= "";
    runCatching {
        latestVersion = getLatestPPVersion().toString()
    }

    return if (latestVersion.isEmpty()) {
        pluginPortal.logger.warning("Could not check for latest version of PluginPortal. Please check manually.")
        true
    } else {
        pluginPortal.description.version == latestVersion
    }
}
