package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.util.delay
import gg.flyte.pluginportal.common.util.HttpInfo
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.SuggestWith
import revxrsal.commands.bukkit.annotation.CommandPermission
import gg.flyte.pluginportal.common.commands.lamp.KeyActionSuggestionProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson

@Command("pp", "pluginportal", "ppm")
class AuthSubCommand {

    private val client = OkHttpClient()
    private val gson = Gson()

    @Subcommand("key")
    @CommandPermission("pluginportal.manage")
    fun key(audience: Audience, @Optional @Named("action") @SuggestWith(KeyActionSuggestionProvider::class) action: String? = null, @Optional @Named("key") key: String? = null) {
        when (action?.lowercase()) {
            "set" -> {
                if (key == null || key.trim().isEmpty()) {
                    val message = status(Status.FAILURE, "Please provide a valid API key")
                        .append(Component.newline())
                        .appendSecondary("Usage: /pp key set <key>")
                    audience.sendMessage(message.boxed())
                    return
                }
                
                val trimmedKey = key.trim()
                
                // Validate key using the API endpoint
                delay(2, true) {
                    val validationResult = validateKey(trimmedKey)
                    
                    if (validationResult.isValid) {
                        // Key is valid, save it
                        val success = Config.setApiKey(trimmedKey)
                        if (!success) {
                            audience.sendMessage(status(Status.FAILURE, "Failed to save API key to config").boxed())
                            return@delay
                        }
                        
                        // Use the saved key for authenticated API requests in this session.
                        try {
                            API.enableAuthenticatedClient(trimmedKey)
                            PluginPortalBase.info.refreshPremiumEntitlement()
                            audience.sendMessage(status(Status.SUCCESS, "API key validated and saved!").boxed())
                        } catch (e: Exception) {
                            audience.sendMessage(status(Status.SUCCESS, "API key validated and saved!").boxed())
                        }
                    } else {
                        audience.sendMessage(status(Status.FAILURE, "Invalid API key").boxed())
                    }
                }
            }
            
            "get" -> {
                val apiKey = Config.getApiKey()
                
                if (apiKey != null) {
                    // Create clickable component to copy the key
                    val keyComponent = Component.text("${maskKey(apiKey)} (click to copy)")
                        .color(net.kyori.adventure.text.format.NamedTextColor.AQUA)
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                            Component.text("Click to copy to clipboard")
                                .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW)
                        ))
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.copyToClipboard(apiKey))
                    
                    audience.sendMessage(keyComponent.boxed())
                } else {
                    audience.sendMessage(status(Status.FAILURE, "No API key configured").boxed())
                }
            }
            
            "clear" -> {
                Config.clearAuthenticationKey()
                API.enableAuthenticatedClient(null)
                PluginPortalBase.info.refreshPremiumEntitlement()
                audience.sendMessage(status(Status.SUCCESS, "API key cleared").boxed())
            }
            
            else -> {
                // Default help menu when no action is specified or action is invalid
                val dashboardLink = Component.text("Plugin Portal Dashboard")
                    .hyperlink(net.kyori.adventure.text.format.NamedTextColor.AQUA)
                    .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                        Component.text("Click to open dashboard")
                            .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW)
                    ))
                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.openUrl("https://pluginportal.link/dashboard"))
                
                val message = status(Status.INFO, "API Key Management")
                    .append(Component.newline())
                    .append(Component.newline())
                    .appendSecondary("Available commands:")
                    .append(Component.newline())
                    // Looks off, it's not really off.
                    .appendSecondary("  /pp key set <key>  - Set and validate API key")
                    .append(Component.newline())
                    .appendSecondary("  /pp key get          - Show current API key status")
                    .append(Component.newline())
                    .appendSecondary("  /pp key clear       - Remove API key")
                    .append(Component.newline())
                    .append(Component.newline())
                    .appendSecondary("Get your API key from the ")
                    .append(dashboardLink)
                
                audience.sendMessage(message.boxed())
            }
        }
    }
    
    private fun validateKey(key: String): ValidationResult {
        return try {
            val url = "${HttpInfo.getApiBaseUrl()}/premium/validate"
            val request = Request.Builder()
                .url(url)
                .post("".toRequestBody())
                .header("x-api-key", key)
                .build()
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val validationResponse = gson.fromJson(responseBody, ValidationResponse::class.java)
                        ValidationResult(validationResponse.valid, validationResponse.message)
                    } else {
                        ValidationResult(false, "Empty response from validation endpoint")
                    }
                } else {
                    ValidationResult(false, "Validation endpoint returned error: ${response.code}")
                }
            }
        } catch (e: Exception) {
            ValidationResult(false, "Could not connect to validation endpoint: ${e.message}")
        }
    }
    
    private data class ValidationResult(
        val isValid: Boolean,
        val message: String?
    )
    
    private data class ValidationResponse(
        val valid: Boolean,
        val message: String?
    )

    private fun maskKey(key: String): String {
        val trimmed = key.trim()
        if (trimmed.length <= 8) return "****"
        return "${trimmed.take(4)}...${trimmed.takeLast(4)}"
    }
}
