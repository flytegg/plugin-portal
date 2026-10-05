package gg.flyte.pluginportal.common.util

import com.google.gson.JsonObject
import gg.flyte.pluginportal.common.PluginPortalBase
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

fun getPolymartDownloadUrl(resourceId: String, token: String? = null): String? = runCatching {
    val url = URL("https://api.polymart.org/v1/getDownloadURL")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

    val postData = buildString {
        append("resource_id=${URLEncoder.encode(resourceId, "UTF-8")}")
        if (!token.isNullOrBlank()) {
            append("&token=${URLEncoder.encode(token, "UTF-8")}")
        }
    }

    connection.outputStream.use { it.write(postData.toByteArray()) }

    if (connection.responseCode != 200) {
        PluginPortalBase.plugin.logger.warning("Polymart download URL request failed with HTTP ${connection.responseCode}")
        return@runCatching null
    }

    val response = connection.inputStream.bufferedReader().use { it.readText() }
    val json = GSON.fromJson(response, JsonObject::class.java)
    if (json.getAsJsonObject("response")?.get("success")?.asBoolean == true) {
        json.getAsJsonObject("response")
            ?.getAsJsonObject("result")
            ?.get("url")
            ?.asString
    } else {
        val error = json.getAsJsonObject("response")?.get("message")?.asString
        PluginPortalBase.plugin.logger.warning("Polymart download URL request failed: ${error ?: "unknown error"}")
        null
    }
}.getOrElse {
    PluginPortalBase.plugin.logger.warning("Polymart download URL request failed: ${it.message ?: it::class.simpleName}")
    null
}
