package gg.flyte.pluginportal.plugin

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.HttpInfo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.bukkit.plugin.java.JavaPlugin
import org.mclicense.library.MCLicense

class EntitlementManager(private val plugin: JavaPlugin) {
    private companion object {
        const val MC_LICENSE_PLUGIN_ID = "676ff1b14cf2cdb257c4ee2d"
        const val PLACEHOLDER_PREFIX = "%%__"
        const val POLYMART_MARKER = "%%__POLYMART__%%"
        const val POLYMART_LICENSE = "%%__LICENSE__%%"
        const val BUILT_BY_BIT_LICENSE = "%%__BBB_LICENSE__%%"
    }

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

        // Fall back to keys embedded by supported marketplace delivery flows.
        val embeddedKey = loadEmbeddedKey()
        if (embeddedKey != null) {
            persistKey(embeddedKey, pluginPortalPath, mcLicensePath)
            return embeddedKey
        }

        return loadMarketplaceKey(mcLicensePath)?.also {
            Config.setApiKey(it)
            plugin.logger.info("Marketplace premium key was imported into plugin configuration.")
        }
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

    private fun loadEmbeddedKey(): String? {
        val entry = plugin.javaClass.classLoader.getResource("mclicense.txt") ?: return null
        val content = entry.readText().trim()
        if (content.isBlank()) return null
        return content.takeUnless { it.startsWith(PLACEHOLDER_PREFIX) }
    }

    private fun loadMarketplaceKey(mcLicensePath: java.io.File): String? {
        val hasBuiltByBitKey = !BUILT_BY_BIT_LICENSE.startsWith(PLACEHOLDER_PREFIX)
        val hasPolymartKey = POLYMART_MARKER == "1" && !POLYMART_LICENSE.startsWith(PLACEHOLDER_PREFIX)
        if (!hasBuiltByBitKey && !hasPolymartKey) return null

        MCLicense.validateKey(plugin, MC_LICENSE_PLUGIN_ID)
        return mcLicensePath.readNonBlankText()
    }

    private fun persistKey(key: String, pluginPortalPath: java.io.File, mcLicensePath: java.io.File) {
        if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdirs()
        val target = if (key.startsWith("pp_")) pluginPortalPath else mcLicensePath
        target.writeText(key)
        Config.setApiKey(key)
        plugin.logger.info("Embedded premium key was migrated into plugin configuration.")
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
