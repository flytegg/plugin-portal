package gg.flyte.common.util


import gg.flyte.common.type.logger.LogType
import gg.flyte.common.type.logger.Logger.log
import gg.flyte.common.type.logger.getStatusType
import okhttp3.Request
import java.net.URL
import javax.net.ssl.HttpsURLConnection

fun getStringFromURL(url: String): String {
    return okHttpClient.newCall(
        Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()
    ).execute().run {
        log(url, code.getStatusType(), LogType.GET)
        this.body?.string().run {
            close()
            this ?: ""
        }
    }

}

fun isDirectDownload(urlString: String): Boolean {
    if (urlString.isEmpty()) return false

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
    if (url.isEmpty()) return false

    val request = Request.Builder()
        .url(getFinalRedirect(url))
        .build()
    val response = okHttpClient.newCall(request).execute().run {
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

private fun getFinalRedirect(url: String): String {
    var request = Request.Builder().url(url).head().build()
    var response = okHttpClient.newCall(request).execute().run {
        log(url, code.getStatusType(), LogType.GET)
        this
    }

    while (response.isRedirect) {
        response.close()
        val redirectUrl: String = response.header("Location") ?: "none"
        request = Request.Builder().url(redirectUrl).head().build()
        response = okHttpClient.newCall(request).execute()
    }

    return response.request.url.toString()
}

//fun <T> makePostRequest(url: String, data: T): String {
//    val request = Request.Builder()
//        .url(url)
//        .method(
//            "POST",
//            objectMapper.writeValueAsString(data).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
//        )
//        .build()
//
//    client.newCall(request).execute().use { response ->
//        log(url, response.code.getStatusType(), LogType.POST)
//
//        if (!response.isSuccessful) throw IOException("Unexpected code $response")
//
//        return response.body?.string().run {
//            response.close()
//            this ?: ""
//        }
//    }
//}