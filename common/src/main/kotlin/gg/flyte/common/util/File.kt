package gg.flyte.common.util

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest

fun getSha256(input: String): String {
    val HEX_CHARS = "0123456789ABCDEF"
    val bytes = MessageDigest
        .getInstance("SHA-256")
        .digest(input.toByteArray())
    val result = StringBuilder(bytes.size * 2)

    bytes.forEach {
        val i = it.toInt()
        result.append(HEX_CHARS[i shr 4 and 0x0f])
        result.append(HEX_CHARS[i and 0x0f])
    }

    return result.toString()
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

fun downloadFileAsync(url: String, destinationFile: File, onComplete: (Boolean) -> Unit) {
    val call: Call<ResponseBody> = apiInterface.downloadFile(url) // Assuming you have a method for file download in your ApiInterface

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
