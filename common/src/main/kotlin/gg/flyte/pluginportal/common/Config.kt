package gg.flyte.pluginportal.common

import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

object Config {
    private lateinit var plugin: JavaPlugin
    private const val DISABLED_DOWNLOAD_PLATFORMS_PATH = "DownloadPlatforms.Disabled"
    private const val DISCORD_WEBHOOK_URL_PATH = "DiscordWebhook.Url"
    private const val TELEMETRY_ENABLED_PATH = "Telemetry.Enabled"
    private val initialized get() = ::plugin.isInitialized
    
    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
        plugin.saveDefaultConfig()
        initDiscordWebhook(plugin)
        initPrivacyDefaults(plugin)
        initFeatures(plugin)
    }

    private fun initDiscordWebhook(plugin: JavaPlugin) {
        if (plugin.config.contains(DISCORD_WEBHOOK_URL_PATH)) return

        plugin.config.set(DISCORD_WEBHOOK_URL_PATH, "")
        plugin.saveConfig()
    }

    private fun initPrivacyDefaults(plugin: JavaPlugin) {
        var changed = false
        val config = plugin.config

        if (!config.contains(TELEMETRY_ENABLED_PATH)) {
            config.set(TELEMETRY_ENABLED_PATH, true)
            changed = true
        }
        if (changed) plugin.saveConfig()
    }

    private fun initFeatures(plugin: JavaPlugin) {
        var changed = false
        val config = plugin.config
        val featureStates = EnumMap<Features, Boolean>(Features::class.java)

        Features.entries.forEach {
            val path = "EnabledFeatures.$it"
            if (config.contains(path)) {
                featureStates[it] = config.getBoolean(path, true)
            } else {
                changed = true
                config.set(path, true)
                featureStates[it] = true
            }
        }

        Features.load(featureStates)

        if (changed) plugin.saveConfig()
    }

    
    fun getString(path: String): String? {
        return plugin.config.getString(path)
    }

    /**
     * Gets all current settings as a structured map that can be sent via WebSocket
     * @return Map containing all configuration settings organized by category
     */
    fun getAllSettings(): Map<String, Any> {
        val settings = mutableMapOf<String, Any>()
        
        // Get all enabled features
        val enabledFeatures = mutableMapOf<String, Boolean>()
        Features.entries.forEach { feature ->
            enabledFeatures[feature.name] = plugin.config.getBoolean("EnabledFeatures.${feature.name}", true)
        }
        settings["EnabledFeatures"] = enabledFeatures
        
        // Get Polymart settings if they exist
        if (plugin.config.contains("Polymart")) {
            val polymartSettings = mutableMapOf<String, Any?>()
            polymartSettings["configured"] = !plugin.config.getString("Polymart.token").isNullOrBlank()
            settings["Polymart"] = polymartSettings
        }

        settings["DownloadPlatforms"] = mapOf(
            "Disabled" to getDisabledDownloadPlatforms().map(MarketplacePlatform::name)
        )
        settings["Telemetry"] = mapOf(
            "Enabled" to isTelemetryEnabled()
        )
        
        return settings
    }

    /**
     * Updates a specific setting dynamically
     * @param path The configuration path (e.g., "EnabledFeatures.INSTALL" or "Polymart.apiKey")
     * @param value The new value for the setting
     * @return true if the update was successful, false otherwise
     */
    fun updateSetting(path: String, value: Any?): Boolean {
        return try {
            // Parse the path to handle nested settings
            val parts = path.split(".")
            
            // Handle feature toggles
            if (parts.size == 2 && parts[0] == "EnabledFeatures") {
                val featureName = parts[1]
                val feature = try {
                    Features.valueOf(featureName)
                } catch (e: IllegalArgumentException) {
                    plugin.logger.warning("Invalid feature name: $featureName")
                    return false
                }
                
                if (value is Boolean) {
                    plugin.config.set(path, value)
                    plugin.saveConfig()
                    
                    // Reload the feature states
                    val featureStates = EnumMap<Features, Boolean>(Features::class.java)
                    Features.entries.forEach {
                        featureStates[it] = plugin.config.getBoolean("EnabledFeatures.$it", true)
                    }
                    Features.load(featureStates)
                    
                    return true
                }
            }
            
            // Handle other settings
            if (path == DISABLED_DOWNLOAD_PLATFORMS_PATH && value is List<*>) {
                val platforms = value.mapNotNull { item ->
                    item?.toString()?.let(MarketplacePlatform::of)?.name
                }
                plugin.config.set(path, platforms)
                plugin.saveConfig()
                return true
            }

            if (path in setOf(
                    TELEMETRY_ENABLED_PATH
                ) && value is Boolean
            ) {
                plugin.config.set(path, value)
                plugin.saveConfig()
                return true
            }

            // Special handling for Polymart token removal
            if (path == "Polymart.token" && value == null) {
                // Remove the entire Polymart section if token is being cleared
                plugin.config.set("Polymart", null)
                plugin.saveConfig()
                return true
            }
            
            // Generic handler for simple values
            plugin.config.set(path, value)
            plugin.saveConfig()
            return true
            
            false
        } catch (e: Exception) {
            plugin.logger.warning("Error updating setting $path: ${e.message}")
            false
        }
    }

    /**
     * Validates a setting value before applying it
     * @param path The configuration path
     * @param value The value to validate
     * @return true if the value is valid for the given path
     */
    fun validateSetting(path: String, value: Any?): Boolean {
        val parts = path.split(".")
        
        // Validate feature toggles
        if (parts.size == 2 && parts[0] == "EnabledFeatures") {
            return value is Boolean && Features.entries.any { it.name == parts[1] }
        }

        if (path == DISABLED_DOWNLOAD_PLATFORMS_PATH) {
            return value is List<*> && value.all { item ->
                item is String && MarketplacePlatform.of(item) != null
            }
        }

        if (path in setOf(
                TELEMETRY_ENABLED_PATH
            )
        ) {
            return value is Boolean
        }
        
        // Validate other settings
        return path == "Polymart.token" && (value == null || value is String)
    }

    fun getDisabledDownloadPlatforms(): Set<MarketplacePlatform> =
        if (!initialized) emptySet() else
        plugin.config.getStringList(DISABLED_DOWNLOAD_PLATFORMS_PATH)
            .mapNotNull(MarketplacePlatform::of)
            .toSet()

    fun isDownloadPlatformEnabled(platform: MarketplacePlatform): Boolean =
        platform !in getDisabledDownloadPlatforms()

    fun getDiscordWebhookUrl(): String? =
        getString(DISCORD_WEBHOOK_URL_PATH)?.trim()?.takeIf { it.isNotEmpty() }

    fun isTelemetryEnabled(): Boolean =
        initialized && plugin.config.getBoolean(TELEMETRY_ENABLED_PATH, true)

    /**
     * Reloads all configuration from disk
     */
    fun reload() {
        plugin.reloadConfig()
        initDiscordWebhook(plugin)
        initPrivacyDefaults(plugin)
        initFeatures(plugin)
    }

    // Authentication key management
    
    /**
     * Sets the API key (any valid authentication key)
     */
    fun setApiKey(key: String): Boolean {
        return if (key.isNotEmpty()) {
            plugin.config.set("Authentication.ApiKey", key)
            plugin.saveConfig()
            true
        } else {
            false
        }
    }
    
    /**
     * Gets the API key
     */
    fun getApiKey(): String? {
        return plugin.config.getString("Authentication.ApiKey")
    }
    
    /**
     * Removes the authentication key
     */
    fun clearAuthenticationKey() {
        plugin.config.set("Authentication", null)
        plugin.saveConfig()
    }

}
