package gg.flyte.common.util

import com.google.common.hash.Hashing
import com.google.common.io.Files
import gg.flyte.common.type.misc.HashType
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

fun File.getSha256Hash() = Files.asByteSource(this).hash(Hashing.sha256()).toString()
fun File.getSha512Hash() = Files.asByteSource(this).hash(Hashing.sha512()).toString()

fun File.getHashes(): HashMap<HashType, String> {
    return HashMap<HashType, String>().apply {
        put(HashType.SHA256, getSha256Hash())
        put(HashType.SHA512, getSha512Hash())
    }
}

//fun download(downloadURL: URL, output: File): File? {
//    return runCatching {
//        log(downloadURL.toString(), StatusType.LOADING, LogType.DOWNLOAD)
//        (downloadURL.openConnection() as? HttpsURLConnection)?.apply {
//            requestMethod = "GET"
//            setRequestProperty("User-Agent", "Mozilla/5.0")
//            connect()
//            inputStream.use { input ->
//                output.outputStream().use { fileOutput ->
//                    input.copyTo(fileOutput)
//                }
//            }
//        }
//        log(downloadURL.toString(), StatusType.OK, LogType.DOWNLOAD)
//        output
//    }.onFailure {
//        it.printStackTrace()
//    }.getOrNull()
//}

fun downloadFileSync(url: String, destinationFile: File): Boolean {
    val call: Call<ResponseBody> = pluginApiInterface.downloadFile(url) // Assuming you have a method for file download in your ApiInterface

    return try {
        val response = call.execute()

        if (response.isSuccessful) {
            val responseBody = response.body()
            responseBody?.let {
                val inputStream = it.byteStream()
                val outputStream = FileOutputStream(destinationFile)
                val buffer = ByteArray(4096)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                true // File download success
            } ?: false // Response body is null
        } else {
            false // Response not successful
        }
    } catch (e: IOException) {
        false // Error during file write or network failure
    }
}


fun downloadFileAsync(url: String, destinationFile: File, onComplete: (Boolean) -> Unit) {
    val call: Call<ResponseBody> = pluginApiInterface.downloadFile(url) // Assuming you have a method for file download in your ApiInterface

    call.enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                val responseBody = response.body()
                responseBody?.let {
                    try {
                        val inputStream = it.byteStream()
                        val outputStream = FileOutputStream(destinationFile)
                        val buffer = ByteArray(4096)
                        var bytesRead: Int

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }

                        outputStream.flush()
                        outputStream.close()
                        inputStream.close()

                        onComplete(true) // File download success
                    } catch (e: IOException) {
                        onComplete(false) // Error during file write
                    }
                } ?: run {
                    onComplete(false) // Response body is null
                }
            } else {
                onComplete(false) // Response not successful
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            onComplete(false) // Network or other failure
        }
    })
}
