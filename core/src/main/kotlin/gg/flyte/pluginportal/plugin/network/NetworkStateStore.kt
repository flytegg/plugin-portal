package gg.flyte.pluginportal.plugin.network

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermissions

/** Only the node identity and bounded operation journal persist across restarts. */
internal class NetworkStateStore(private val directory: File) {
    private val credentialFile = File(directory, "network-node.json")
    private val journalFile = File(directory, "network-operations.json")

    fun readIdentity(): JsonObject? = read(credentialFile, 4095)?.also {
        require(Regex("^ppn_[A-Za-z0-9_-]{43}$").matches(it.get("credential").asString)) { "Invalid node credential" }
        require(validId(it.get("nodeId").asString) && validId(it.get("networkId").asString)) { "Invalid node identity" }
        require(it.get("role").asString in setOf("node", "controller")) { "Invalid node role" }
    }
    fun readJournal(): JsonObject? = read(journalFile, 4 * 1024 * 1024)
    fun saveIdentity(identity: JsonObject) = write(credentialFile, identity)
    fun saveJournal(journal: JsonObject) = write(journalFile, journal)
    fun deleteIdentity() { safe(credentialFile); Files.deleteIfExists(credentialFile.toPath()) }

    private fun read(file: File, maximum: Int): JsonObject? {
        safe(file)
        if (!file.exists()) return null
        require(file.length() <= maximum) { "Network file is too large" }
        return JsonParser.parseString(file.readText()).asJsonObject
    }
    private fun safe(file: File) {
        require(!Files.isSymbolicLink(file.toPath()) && (!file.exists() || Files.isRegularFile(file.toPath(), LinkOption.NOFOLLOW_LINKS))) { "Unsafe network file" }
    }
    private fun write(file: File, value: JsonObject) {
        safe(file)
        directory.mkdirs()
        val temporary = Files.createTempFile(directory.toPath(), ".network-", ".json")
        try {
            if (Files.getFileStore(temporary).supportsFileAttributeView("posix")) Files.setPosixFilePermissions(temporary, PosixFilePermissions.fromString("rw-------"))
            FileChannel.open(temporary, StandardOpenOption.WRITE).use { channel ->
                val buffer = java.nio.ByteBuffer.wrap(value.toString().toByteArray(Charsets.UTF_8))
                while (buffer.hasRemaining()) channel.write(buffer)
                channel.force(true)
            }
            Files.move(temporary, file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally { Files.deleteIfExists(temporary) }
    }

    companion object {
        fun validId(value: String) = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$").matches(value)
    }
}
