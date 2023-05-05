package link.portalbox.pluginportal.util

import link.portalbox.pluginportal.type.GameVersion
import link.portalbox.pplib.type.MarketplacePlugin
import link.portalbox.pplib.type.RequestPlugin
import org.apache.commons.lang.StringUtils.startsWithIgnoreCase
import org.apache.commons.lang.Validate
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

// Convert MarketplacePlugin to RequestPlugin, with an input of "reasonForRequest"
fun MarketplacePlugin.toRequestPlugin(reasonForRequest: String): RequestPlugin {
    return RequestPlugin(id, service, name, reasonForRequest)
}

fun copyPartialMatchesWithService(
    input: String,
    originals: Iterable<String>,
    matches: MutableCollection<String>
): MutableCollection<String> {
    Validate.notNull(input, "Search token cannot be null")
    Validate.notNull(matches, "Collection cannot be null")
    Validate.notNull(originals, "Originals cannot be null")

    for (string in originals) {
        if (startsWithIgnoreCase(string.split(":")[1], input)) {
            matches.add("${string.split(":")[0].lowercase()}:${string.split(":")[1]}")
        }
    }

    return matches
}