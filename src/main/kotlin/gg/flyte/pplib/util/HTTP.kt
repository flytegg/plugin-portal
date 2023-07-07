package gg.flyte.pplib.util

import gg.flyte.pplib.type.logger.LogType
import gg.flyte.pplib.type.logger.StatusType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

val client = OkHttpClient.Builder()
    .build()

fun getStringFromURL(url: String): String {
    return client.newCall(
        Request.Builder()
            .url(url)
            .header("User-Agent", "flyte/pp-lib")
            .build()
    ).execute().run {
        log(url, code.getStatusType(), LogType.GET)
        this.body.string().run {
            close()
            this
        }
    }

}

fun isDirectDownload(urlString: String): Boolean {
    return runCatching {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod = "HEAD"
        connection.connect()

        val contentType = connection.contentType
        val contentLength = connection.contentLength

        connection.disconnect()
        log(urlString, connection.responseCode.getStatusType(), LogType.GET)

        contentType == "application/octet-stream" && contentLength != -1
    }.getOrDefault(false)
}

fun isJARFileDownload(url: String): Boolean {
    val request = Request.Builder()
        .url(getFinalRedirect(url))
        .build()
    val response = client.newCall(request).execute().run {
        log(url, code.getStatusType(), LogType.GET)
        this
    }

    return runCatching {
        val contentType = response.header("Content-Type")
        val contentDisposition = response.header("Content-Disposition")

        return (contentType != null && contentType == "application/java-archive")
                || (contentDisposition != null && contentDisposition.contains(".jar"))
                || (url.endsWith(".jar"))
    }.getOrDefault(false)
}

fun getFinalRedirect(url: String): String {
    var request = Request.Builder().url(url).head().build()
    var response = client.newCall(request).execute().run {
        log(url, code.getStatusType(), LogType.GET)
        this
    }

    while (response.isRedirect) {
        response.close()
        val redirectUrl: String = response.header("Location") ?: "none"
        request = Request.Builder().url(redirectUrl).head().build()
        response = client.newCall(request).execute()
    }

    return response.request.url.toString()
}

fun <T> makePostRequest(url: String, data: T): String {
    val request = Request.Builder()
        .url(url)
        .method(
            "POST",
            objectMapper.writeValueAsString(data).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        )
        .build()

    client.newCall(request).execute().use { response ->
        log(url, response.code.getStatusType(), LogType.POST)

        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        return response.body.string().run {
            response.close()
            this
        }
    }
}

fun Int.getStatusType(): StatusType {
    when (this) {
        in 100..199 -> return StatusType.LOADING
        in 200..299 -> return StatusType.OK
        in 300..399 -> return StatusType.WARNING
        in 400..499 -> return StatusType.ERROR
        in 500..599 -> return StatusType.ERROR
    }

    return StatusType.ERROR
}