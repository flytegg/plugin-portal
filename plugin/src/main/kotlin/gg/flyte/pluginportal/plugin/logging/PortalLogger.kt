package gg.flyte.pluginportal.plugin.logging

import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.util.appendLine
import gg.flyte.pluginportal.plugin.util.async
import gg.flyte.pluginportal.plugin.util.createIfNotExists
import gs.mclo.api.MclogsClient
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import org.bukkit.command.CommandSender
import java.io.File
import java.util.logging.Level
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object PortalLogger {
    private const val MAX_ENTRIES = 1000
    private const val ARCHIVE_FOLDER = "archive"
    private val dataFolder = PluginPortal.instance.dataFolder
    private var currentFile = File(dataFolder, "history.log").apply { createNewFile() }
    private var entryCount = 0

    init {
        loadExistingEntries()
    }

    fun info(action: Action, message: String) = log(Record(System.currentTimeMillis(), "SYSTEM", action, message))

    fun log(initiator: Audience, action: Action, target: String) {
        val name: String = initiator.getOrDefault(Identity.NAME, "[UNKNOWN]")!!
        log(Record(System.currentTimeMillis(), name, action, target))
    }

    fun log(initiator: CommandSender, action: Action, target: String) =
        log(Record(System.currentTimeMillis(), initiator.name, action, target))

    fun log(record: Record) {
        PluginPortal.instance.logger.log(Level.INFO, record.timelessDescription)
        writeToFile(record)
    }

    private fun writeToFile(record: Record) {
        currentFile.appendLine(record.description)
        entryCount++

        if (entryCount >= MAX_ENTRIES) {
            archiveCurrentFile()
            createNewFile()
        }
    }

    private fun archiveCurrentFile() {
        val archiveFolder = File(dataFolder, ARCHIVE_FOLDER).apply { mkdirs() }
        val archiveFile = File(archiveFolder, "history_${System.currentTimeMillis()}.zip")

        ZipOutputStream(archiveFile.outputStream()).use { zipOut ->
            zipOut.putNextEntry(ZipEntry(currentFile.name))
            currentFile.inputStream().use { input ->
                input.copyTo(zipOut)
            }
        }

        currentFile.delete()
    }

    private fun createNewFile() {
        currentFile = File(dataFolder, "history.log").apply { createNewFile() }
        entryCount = 0
    }

    private fun loadExistingEntries() {
        entryCount = currentFile.readLines().size
    }

    fun query(filter: (Record) -> Boolean): List<Record> {
        val records = mutableListOf<Record>()

        // read current
        currentFile.readLines().mapNotNull { parseRecord(it) }.filter(filter).let { records.addAll(it) }

        // read from archive
        File(dataFolder, ARCHIVE_FOLDER).listFiles { file -> file.extension == "zip" }?.forEach { zipFile ->
            ZipFile(zipFile).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    zip.getInputStream(entry).bufferedReader().useLines { lines ->
                        lines.mapNotNull { parseRecord(it) }.filter(filter).let { records.addAll(it) }
                    }
                }
            }
        }

        return records
    }

    private fun parseRecord(line: String): Record? {
        val parts = line.split("|")
        if (parts.size != 4) return null
        return try {
            Record(
                parts[0].toLong(),
                parts[1],
                Action.valueOf(parts[2]),
                parts[3]
            )
        } catch (e: Exception) {
            null
        }
    }

    enum class Action {
        // Actions are queried linearly thus AUTO_UPDATE must precede UPDATE etc.
        INITIATED_INSTALL, FAILED_INSTALL, INSTALL,
        NOTICED_DELETE, DELETE,
        AUTO_UPDATE, INITIATED_UPDATE, FAILED_UPDATE, UPDATE,
        LOAD_PLUGINS, SAVE_PLUGINS;
    }
}