package gg.flyte.pluginportal.common.util

import gg.flyte.pluginportal.common.types.enums.ServerType
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** Validate before publishing, so a failed transfer never corrupts an installed JAR. */
fun downloadJar(url: URL, destination: File, serverTypes: List<ServerType>, expectedSha256: String? = null, apiKey: String? = null): File {
    require(url.protocol == "https" || (java.lang.Boolean.getBoolean("pluginportal.dev") && url.protocol == "http")) { "Downloads require HTTPS" }
    require(!Files.isSymbolicLink(destination.toPath())) { "Refusing a symbolic-link destination" }
    destination.absoluteFile.parentFile.mkdirs()
    val temporary = Files.createTempFile(destination.absoluteFile.parentFile.toPath(), ".download-", ".jar")
    try {
        var location = url
        var completed = false
        for (redirect in 0..5) {
            val connection = location.openConnection() as HttpURLConnection
            connection.connectTimeout = 15_000
            connection.readTimeout = 60_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("User-Agent", "PluginPortal")
            if (apiKey != null && location.protocol == url.protocol && location.host == url.host && location.port == url.port) {
                connection.setRequestProperty("x-api-key", apiKey)
            }
            try {
                if (connection.responseCode in 300..399) {
                    location = URL(location, requireNotNull(connection.getHeaderField("Location")))
                    require(location.protocol == "https" || (java.lang.Boolean.getBoolean("pluginportal.dev") && location.protocol == "http")) { "Insecure download redirect" }
                    continue
                }
                require(connection.responseCode == 200) { "Download returned HTTP ${connection.responseCode}" }
                val limit = 256L * 1024 * 1024
                require(connection.contentLengthLong <= limit) { "Plugin exceeds the 256 MiB limit" }
                connection.inputStream.use { input ->
                    Files.newOutputStream(temporary).use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var total = 0L
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            total += count
                            require(total <= limit) { "Plugin exceeds the 256 MiB limit" }
                            output.write(buffer, 0, count)
                        }
                    }
                }
                completed = true
                break
            } finally { connection.disconnect() }
        }
        require(completed) { "Too many download redirects" }
        val file = temporary.toFile()
        file.requireCompatibleJar(serverTypes)
        if (expectedSha256 != null) {
            require(Regex("[0-9a-fA-F]{64}").matches(expectedSha256) && HashType.SHA256.hash(file).equals(expectedSha256, true)) { "Downloaded JAR checksum does not match" }
        }
        Files.move(temporary, destination.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        return destination
    } finally { Files.deleteIfExists(temporary) }
}
