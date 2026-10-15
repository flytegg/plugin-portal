package gg.flyte.pluginportal.plugin

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.HttpInfo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import gg.flyte.pluginportal.common.runtime.PortalRuntime

class EntitlementManager(private val plugin: PortalRuntime) {
    private var state: EntitlementState = EntitlementState.MissingKey

    private data class PremiumValidationResponse(val valid: Boolean, val message: String?)

    fun loadConfiguredKey(): String? {
        // Prefer the current config key location.
        Config.getApiKey()?.takeIf { it.isNotBlank() }?.let {
            plugin.logger.info("Using API key from config for premium entitlement.")
            return it
        }

        // Preserve compatibility with legacy key files.
        val mcLicensePath = plugin.dataFolder.resolve("mclicense.txt")
        val pluginPortalPath = plugin.dataFolder.resolve("pluginportal.txt")

        pluginPortalPath.readNonBlankText()?.let {
            plugin.logger.info("Plugin Portal API key found in pluginportal.txt")
            Config.setApiKey(it)
            return it
        }

        mcLicensePath.readNonBlankText()?.let {
            plugin.logger.info("Legacy MCLicense key found in mclicense.txt")
            Config.setApiKey(it)
            return it
        }

        // Marketplace downloads carry the buyer's key. It unlocks premium commands only;
        // it never links a server to a network.
        loadEmbeddedKey()?.let {
            persistKey(it, if (it.startsWith("pp_")) pluginPortalPath else mcLicensePath)
            return it
        }
        return MarketplaceKey.embedded()?.also { persistKey(it, mcLicensePath) }
    }

    private fun loadEmbeddedKey(): String? {
        val entry = EntitlementManager::class.java.classLoader.getResource("mclicense.txt") ?: return null
        return entry.readText().trim().takeUnless { it.isEmpty() || it.startsWith("%%__") }
    }

    private fun persistKey(key: String, target: java.io.File) {
        target.parentFile.mkdirs()
        target.writeText(key)
        Config.setApiKey(key)
        plugin.logger.info("Marketplace premium key was imported into plugin configuration.")
    }

    fun refresh(): EntitlementState {
        val apiKey = Config.getApiKey()?.trim()?.takeIf { it.isNotEmpty() }
        if (apiKey == null) {
            state = EntitlementState.MissingKey
            return state
        }

        API.enableAuthenticatedClient(apiKey)
        state = validate(apiKey)
        return state
    }

    fun hasPremiumAccess(): Boolean = state is EntitlementState.Valid

    fun lockedMessage(): String = when (val current = state) {
        EntitlementState.MissingKey -> "Premium features require a Plugin Portal key. Run /pp key set <key> to configure one."
        is EntitlementState.Invalid -> current.message ?: "The configured Plugin Portal key is not valid for premium features."
        is EntitlementState.ApiUnavailable -> "Plugin Portal could not verify premium access right now. Try again once the API is reachable."
        EntitlementState.Valid -> "Premium access is active."
    }

    private fun validate(apiKey: String): EntitlementState {
        val url = "${HttpInfo.getApiBaseUrl()}/premium/validate"
        return try {
            val request = Request.Builder()
                .url(url)
                .post("{}".toRequestBody("application/json".toMediaType()))
                .header("x-api-key", apiKey)
                .build()

            OkHttpClient().newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val parsed = runCatching {
                    GSON.fromJson(body, PremiumValidationResponse::class.java)
                }.getOrNull()

                if (response.code in 200..299 && parsed?.valid == true) {
                    plugin.logger.info("Premium entitlement verified.")
                    EntitlementState.Valid
                } else {
                    plugin.logger.warning("Premium entitlement check failed (HTTP ${response.code}). Premium actions will stay locked.")
                    EntitlementState.Invalid(parsed?.message)
                }
            }
        } catch (e: Exception) {
            plugin.logger.warning("Premium entitlement check failed: ${e.message ?: e::class.simpleName}")
            EntitlementState.ApiUnavailable(e.message ?: e::class.simpleName ?: "unknown")
        }
    }

    private fun java.io.File.readNonBlankText(): String? {
        if (!exists()) return null
        return readText().trim().takeIf { it.isNotEmpty() }
    }
}

sealed interface EntitlementState {
    data object MissingKey : EntitlementState
    data object Valid : EntitlementState
    data class Invalid(val message: String?) : EntitlementState
    data class ApiUnavailable(val reason: String) : EntitlementState
}
