package gg.flyte.pplib.util

import gg.flyte.pplib.type.api.PostError
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.logging.Level
import java.util.logging.Logger

private const val LOGGER_NAME = "Plugin Portal"
lateinit var defaultPostError : PostError

/**
 * Logs a message with the given logging level.
 *
 * @param level the logging level
 * @param message the message to be logged
 */
fun log(level: Level, message: String) {
    log(level, message, false)
}

/**
 * Logs a message with the given logging level and saves it to the log file if specified.
 *
 * @param level the logging level
 * @param message the message to be logged
 * @param saveToLog flag indicating whether to save the message to the log file
 */
fun log(level: Level, message: String, saveToLog: Boolean) {
    Logger.getLogger(LOGGER_NAME).log(level, message)
    if (saveToLog) saveToLog(message)
}

/**
 * Logs a message with the given logging level and throwable, and saves it to the log file.
 *
 * @param level the logging level
 * @param message the message to be logged
 * @param throwable the throwable to be logged
 */
fun log(level: Level, message: String, throwable: Throwable) {
    Logger.getLogger(LOGGER_NAME).log(level, message, throwable)
    saveToLog("$message ERROR: ${throwable.message}")
}

/**
 * Returns the log file.
 * If the file does not exist, creates it and renames the existing latest.txt file.
 * If the file cannot be created or renamed, logs an error message.
 *
 * @return The log file
 */
private fun getLogFile(): File {
    val folder = File("PP-Library").apply { mkdirs() }

    // Rename existing log file
    val latestFile = File(folder, "latest.txt")
    if (latestFile.exists()) {
        val fileNames = folder.listFiles { file ->
            file.name.startsWith("ppLog-") && file.name.endsWith(".txt")
        }?.map { file ->
            file.name.substring(6, file.name.length - 4).toIntOrNull() ?: 0
        } ?: emptyList()

        val highestNumber = fileNames.maxOrNull() ?: 0
        val newLogFile = File(folder, "ppLog-${highestNumber + 1}.txt")
        if (!latestFile.renameTo(newLogFile)) {
            runCatching {
                log(Level.SEVERE, "Could not rename latest.txt to ${newLogFile.name}", true)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    // Create latest log file if it doesn't exist
    val latestLogFile = File(folder, "latest.txt")
    if (!latestLogFile.exists()) {
        runCatching {
            latestLogFile.createNewFile()
        }.onFailure {
            log(
                Level.SEVERE,
                "Could not create ${latestLogFile.name}, please report this to our discord: discord.gg/portalbox",
                true
            )
            it.printStackTrace()
        }
    }

    return latestLogFile
}

/**
 * Saves the given [message] to the log file.
 *
 * @param message the message to be saved to the log file
 */
private fun saveToLog(message: String) {
    val file = getLogFile()

    runCatching {
        BufferedWriter(FileWriter(file, true)).use { writer ->
            writer.write(message)
            writer.newLine()
            writer.flush()
        }
    }.onFailure {
        log(Level.SEVERE, "Could not write to log file, Please report this to our discord: discord.gg/pluginportal", true)
        it.printStackTrace()
    }
}

