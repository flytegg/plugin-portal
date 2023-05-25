package gg.flyte.pplib.util

import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection

/**
 * Computes the SHA-256 hash of the given file.
 *
 * @param file the file to compute the hash for
 * @return the SHA-256 hash of the file as a hexadecimal string, or null if the computation failed
 */
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

/**
 * Downloads a file from a given URL and saves it to a specified location on the file system.
 *
 * @param downloadURL The URL of the file to be downloaded.
 * @param output The file to which the downloaded content will be saved.
 * @return The downloaded file, or null if the download failed.
 */
fun download(downloadURL: URL, output: File): File? {
    return runCatching {
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
        output
    }.onFailure {
        it.printStackTrace()
    }.getOrNull()
}
