package gg.flyte.pplib.util

import gg.flyte.pplib.type.logger.LogType
import gg.flyte.pplib.type.logger.StatusType
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection

fun getSHA256(file: File): String? {
    val messageDigest = runCatching {
        MessageDigest.getInstance("SHA-256")
    }.onFailure {
        it.printStackTrace()
    }.getOrNull()

    runCatching {
        FileInputStream(file).use { inputStream ->
            val dataBytes = ByteArray(1024)

            var length = inputStream.read(dataBytes)
            while (length != -1) {
                messageDigest?.update(dataBytes, 0, length)
                length = inputStream.read(dataBytes)
            }
        }
    }.onFailure {
        it.printStackTrace()
    }.getOrNull()

    val digest = messageDigest?.digest()
    return digest?.joinToString("") { "%02x".format(it) }
}

fun download(downloadURL: URL, output: File): File? {
    return runCatching {
        log(downloadURL.toString(), StatusType.LOADING, LogType.DOWNLOAD)
        (downloadURL.openConnection() as? HttpsURLConnection)?.apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "Mozilla/5.0")
            connect()
            inputStream.use { input ->
                output.outputStream().use { fileOutput ->
                    input.copyTo(fileOutput)
                }
            }
        }
        log(downloadURL.toString(), StatusType.OK, LogType.DOWNLOAD)
        output
    }.onFailure {
        it.printStackTrace()
    }.getOrNull()
}