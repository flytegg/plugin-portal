package gg.flyte.pluginportal.common.types

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// Core message structures
data class WSMessage<T>(
    val id: String,
    val type: String,
    val payload: T,
    val meta: MessageMeta? = null
)

data class MessageMeta(
    val timestamp: Long,
    val correlationId: String? = null
)

data class WSResponse<T>(
    val id: String,
    val type: String,
    val result: WSResult<T>
)

sealed class WSResult<out T> {
    data class Success<T>(
        val data: T
    ) : WSResult<T>() {
        @SerializedName("status")
        val status: String = "success"
    }
    
    data class Error(
        val error: WSError
    ) : WSResult<Nothing>() {
        @SerializedName("status")
        val status: String = "error"
    }
}

data class WSError(
    val code: String,
    val message: String,
    val details: Map<String, JsonElement>? = null
)

// Message type registry
object MessageTypes {
    // Queries
    const val LOCAL_PLUGINS = "query.localPlugins"
    const val GET_SETTINGS = "query.getSettings"
    
    // Mutations
    const val INSTALL_PLUGIN = "mutation.installPlugin"
    const val UNINSTALL_PLUGIN = "mutation.uninstallPlugin"
    const val UPDATE_PLUGIN = "mutation.updatePlugin"
    const val TOGGLE_PLUGIN_BLACKLIST = "mutation.togglePluginBlacklist"
    const val SWITCH_PLUGIN_PLATFORM = "mutation.switchPluginPlatform"
    const val UPDATE_SETTING = "mutation.updateSetting"
    
    // Events
    const val CONNECTION_STATUS = "event.connectionStatus"
    const val HEARTBEAT = "event.heartbeat"
}

// Shared types
data class TypedLocalPlugin(
    val platformId: String,
    val name: String,
    val version: String,
    val platform: String, // Using string for cross-platform compatibility
    val sha256: String,
    val sha512: String,
    val installedAt: Long,
    val preferredChannel: String? = null,
    val excludedFromUpdates: Boolean = false
)

data class PluginIdentifier(
    val platform: String,
    val id: String,
    val version: String? = null,
    val channel: String? = null,
    val targetPlatform: String? = null
)

data class PluginOperationResult(
    val platform: String,
    val id: String,
    val operation: String
)

data class ConnectionStatus(
    val connections: ConnectionCounts,
    val userType: String? = null,
    val action: String? = null
)

data class ConnectionCounts(
    val web: Int,
    val plugin: Int
)

data class Settings(
    @SerializedName("EnabledFeatures")
    val enabledFeatures: EnabledFeatures,
    @SerializedName("Polymart")
    val polymart: PolymartSettings? = null
)

data class PolymartSettings(
    val configured: Boolean = false
)

data class EnabledFeatures(
    @SerializedName("INSTALL")
    val install: Boolean,
    @SerializedName("UPDATE")
    val update: Boolean,
    @SerializedName("DELETE")
    val delete: Boolean,
    @SerializedName("LIST")
    val list: Boolean,
    @SerializedName("RECOGNISE")
    val recognise: Boolean,
    @SerializedName("IMPORT")
    val import: Boolean,
    @SerializedName("EXPORT")
    val export: Boolean,
    @SerializedName("AUTOMATICALLY_UPDATE_PPP")
    val automaticallyUpdatePpp: Boolean
)


// Error codes
object ErrorCodes {
    const val TIMEOUT = "TIMEOUT"
    const val INVALID_MESSAGE = "INVALID_MESSAGE"
    const val PLUGIN_NOT_FOUND = "PLUGIN_NOT_FOUND"
    const val INSTALL_FAILED = "INSTALL_FAILED"
    const val UNINSTALL_FAILED = "UNINSTALL_FAILED"
    const val UPDATE_FAILED = "UPDATE_FAILED"
    const val BLACKLIST_FAILED = "BLACKLIST_FAILED"
    const val PLATFORM_SWITCH_FAILED = "PLATFORM_SWITCH_FAILED"
    const val INVALID_SETTING_PATH = "INVALID_SETTING_PATH"
    const val PLUGIN_CACHE_ERROR = "PLUGIN_CACHE_ERROR"
    const val UNAUTHORIZED = "UNAUTHORIZED"
    const val INTERNAL_ERROR = "INTERNAL_ERROR"
}

// Action response for plugin operations
data class PluginActionResponse(
    val success: Boolean,
    val message: String? = null
)
