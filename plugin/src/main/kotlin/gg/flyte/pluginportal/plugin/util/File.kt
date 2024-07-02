package gg.flyte.pluginportal.plugin.util

import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

fun isValidDownload(url: String): Boolean {
    return isJarDownloadUrl(url)
}

fun isJarDownloadUrl(url: String): Boolean {
    if (url.endsWith(".jar")) return true

    val connection = runCatching { URL(url).openConnection() as HttpURLConnection }.getOrNull() ?: return false
    val fileName = connection.getHeaderField("x-bz-file-name") ?: return false
    if (fileName.endsWith(".jar")) return true

    connection.instanceFollowRedirects = false
    val contentDisposition = connection.getHeaderField("Content-Disposition")
    return contentDisposition?.let {
        it.contains("attachment") && it.contains("filename=") && it.contains(".jar")
    } ?: false
}

fun File.isJarFile() = isFile && extension == "jar"

fun hash(data: ByteArray, algo: String = "SHA-256"): String {
    return MessageDigest
        .getInstance(algo)
        .digest(data)
        .joinToString("") { byte -> "%02x".format(byte) }
}

private fun calculateSHA256(file: File): String = hash(file.readBytes())
private fun calculateSHA1(file: File): String = hash(file.readBytes(), "SHA-1")
private fun calculateSHA512(file: File): String = hash(file.readBytes(), "SHA-512")

enum class HashType(val hash: (File) -> String) {
    SHA256(::calculateSHA256),
    SHA1(::calculateSHA1),
    SHA512(::calculateSHA512)
}