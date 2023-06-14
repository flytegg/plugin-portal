package link.portalbox.pluginportal.util

import gg.flyte.pplib.type.error.PostError
import gg.flyte.pplib.type.plugin.MarketplacePlugin
import gg.flyte.pplib.type.plugin.RequestPlugin
import gg.flyte.pplib.type.service.ServiceType
import link.portalbox.pluginportal.type.GameVersion
import org.bukkit.util.StringUtil.startsWithIgnoreCase
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

lateinit var defaultPostError: PostError

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
fun MarketplacePlugin.toRequestPlugin(reasonForRequest: String, username: String): RequestPlugin {
    val externalURL = if (service == ServiceType.SPIGOTMC) {
        "https://www.spigotmc.org/resources/$id/"
    } else {
        "https://hangar.papermc.io/ (Download will be added soon)"
    }

    return RequestPlugin(id, service, reasonForRequest, name, externalURL, username)
}

fun copyPartialMatchesWithService(
    input: String,
    originals: Iterable<String>,
    matches: MutableCollection<String>
): MutableCollection<String> {

    for (string in originals) {
        if (startsWithIgnoreCase(string.split(":")[1], input)) {
            matches.add("${string.split(":")[0].lowercase()}:${string.split(":")[1]}")
        }
    }

    return matches
}