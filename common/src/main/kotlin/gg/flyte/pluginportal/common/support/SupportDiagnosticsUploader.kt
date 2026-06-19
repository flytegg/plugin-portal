package gg.flyte.pluginportal.common.support

import com.google.gson.JsonObject
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.HttpInfo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPOutputStream

data class SupportUploadResult(
    val success: Boolean,
    val bundleId: String? = null,
    val expiresAt: String? = null,
    val message: String? = null
)

object SupportDiagnosticsUploader {
    private val client = OkHttpClient()

    fun upload(code: String, payload: SupportDiagnosticsPayload): SupportUploadResult {
        val payloadJson = GSON.toJson(payload)
        val compressedPayload = gzipBase64(payloadJson)
        val body = GSON.toJson(
            mapOf(
                "code" to code,
                "payloadVersion" to payload.payloadVersion,
                "compressedPayloadBase64" to compressedPayload
            )
        )

        val request = Request.Builder()
            .url("${HttpInfo.getApiBaseUrl()}/support/diagnostics/upload")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        return client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            val json = runCatching { GSON.fromJson(responseBody, JsonObject::class.java) }.getOrNull()

            if (response.isSuccessful && json?.get("success")?.asBoolean == true) {
                SupportUploadResult(
                    success = true,
                    bundleId = json.get("bundleId")?.asString,
                    expiresAt = json.get("expiresAt")?.asString
                )
            } else {
                SupportUploadResult(
                    success = false,
                    message = json?.get("message")?.asString ?: "Support upload failed with HTTP ${response.code}"
                )
            }
        }
    }

    private fun gzipBase64(text: String): String {
        val bytes = ByteArrayOutputStream()
        GZIPOutputStream(bytes).use { it.write(text.toByteArray(Charsets.UTF_8)) }
        return Base64.getEncoder().encodeToString(bytes.toByteArray())
    }
}
