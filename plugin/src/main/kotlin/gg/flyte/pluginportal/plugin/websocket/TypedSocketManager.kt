package gg.flyte.pluginportal.plugin.websocket

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import gg.flyte.pluginportal.common.logging.PortalLogger
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.types.*
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.HttpInfo
import com.google.gson.Gson
import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.plugin.PluginPortal
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object TypedSocketManager {

    private var client: WebSocketClient? = null
    private var reconnectExecutor = Executors.newSingleThreadScheduledExecutor()
    private var isEnabled = false
    private var hasConnectedOnce = false
    private lateinit var secret: String
    private lateinit var roomCode: String
    private var currentURL: String? = null
    private var connectionStartTime: Long = 0
    private var lastActivityTime: Long = 0
    private var reconnectScheduled = false
    private var connectedUsers: MutableList<String> = mutableListOf()
    private val wsGson: Gson = createWSGson()
    private var lastLoggedWebCount = -1
    private var lastLoggedPluginCount = -1

    fun start(secret: String, roomCode: String, editorURL: String? = null) {
        if (hasConnectedOnce) return
        if (reconnectExecutor.isShutdown || reconnectExecutor.isTerminated) {
            reconnectExecutor = Executors.newSingleThreadScheduledExecutor()
        }

        this.secret = secret
        this.roomCode = roomCode
        this.currentURL = editorURL

        isEnabled = true
        hasConnectedOnce = true
        connectToWebSocket()
    }

    fun isConnected(): Boolean = client?.isOpen == true

    fun hasConnectedBefore(): Boolean = hasConnectedOnce

    fun stop() {
        isEnabled = false
        hasConnectedOnce = false
        currentURL = null
        connectionStartTime = 0
        lastActivityTime = 0
        reconnectScheduled = false
        connectedUsers.clear()
        lastLoggedWebCount = -1
        lastLoggedPluginCount = -1
        client?.close()
        reconnectExecutor.shutdown()
    }

    fun getCurrentURL(): String? = currentURL

    fun getConnectionDuration(): Long = if (connectionStartTime > 0) {
        System.currentTimeMillis() - connectionStartTime
    } else 0

    fun getTimeSinceLastActivity(): Long = if (lastActivityTime > 0) {
        System.currentTimeMillis() - lastActivityTime
    } else 0

    fun getConnectedUsers(): List<String> = connectedUsers.toList()

    fun getRoomCode(): String? = if (this::roomCode.isInitialized) roomCode else null

    fun reconnect() {
        if (!this::secret.isInitialized || !this::roomCode.isInitialized) {
            throw IllegalStateException("No previous session data available for reconnection")
        }
        
        if (isConnected()) {
            return
        }
        
        isEnabled = true
        connectToWebSocket()
    }

    private fun updateActivity() {
        lastActivityTime = System.currentTimeMillis()
    }

    val actions = SocketActions()

    private fun connectToWebSocket() {
        if (!isEnabled) return

        try {
            val wsUrl = if (roomCode == "server") {
                "${HttpInfo.getSocketBaseUrl()}/ws/server"
            } else {
                "${HttpInfo.getSocketBaseUrl()}/ws/temp/$roomCode?token=$secret&role=server"
            }
            val redactedUrl = if (roomCode == "server") {
                "${HttpInfo.getSocketBaseUrl()}/ws/server"
            } else {
                "${HttpInfo.getSocketBaseUrl()}/ws/temp/$roomCode?token=<redacted>&role=server"
            }
            PortalLogger.info(PortalLogger.Action.WEBSOCKET, "Attempting WebSocket connection to: $redactedUrl")
            val uri = URI(wsUrl)
            val headers = if (roomCode == "server") mapOf("x-api-key" to secret) else emptyMap()

            client = object : WebSocketClient(uri, headers) {
                override fun onOpen(handshake: ServerHandshake?) {
                    PortalLogger.info(PortalLogger.Action.WEBSOCKET, "Connected to plugin portal websocket")
                    reconnectScheduled = false
                    connectionStartTime = System.currentTimeMillis()
                    lastActivityTime = System.currentTimeMillis()
                }

                override fun onMessage(message: String?) {
                    if (message == null) return
                    
                    updateActivity()

                    try {
                        val jsonElement = GSON.fromJson(message, JsonElement::class.java)
                        if (jsonElement.isJsonObject) {
                            val jsonObject = jsonElement.asJsonObject
                            
                            if (jsonObject.has("id") && jsonObject.has("type")) {
                                handleTypedMessage(jsonObject)
                            } else if (jsonObject.has("system")) {
                                handleSystemMessage(jsonObject)
                            } else {
                                PortalLogger.warn("Unknown websocket message format")
                            }
                        }
                    } catch (e: Exception) {
                        PortalLogger.error("Error processing websocket message: ${e.message ?: e::class.simpleName}")
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    PortalLogger.info(
                        PortalLogger.Action.WEBSOCKET,
                        "Disconnected from plugin portal websocket (code $code${reason?.let { ", reason: $it" } ?: ""})"
                    )

                    if (isEnabled && shouldReconnectAfterClose(code)) {
                        scheduleReconnect()
                    }
                }

                override fun onError(ex: Exception?) {
                    PortalLogger.error("WebSocket error: ${ex?.message ?: ex?.javaClass?.simpleName ?: "unknown error"}")
                    if (isEnabled) {
                        scheduleReconnect()
                    }
                }
            }

            client?.connect()

        } catch (e: Exception) {
            PortalLogger.error("Failed to connect to websocket: ${e.message ?: e::class.simpleName}")
            if (isEnabled) {
                scheduleReconnect()
            }
        }
    }

    private fun handleTypedMessage(jsonObject: JsonObject) {
        val message = WSMessage(
            id = jsonObject.get("id").asString,
            type = jsonObject.get("type").asString,
            payload = jsonObject.get("payload") ?: JsonObject(),
            meta = jsonObject.get("meta")?.let { 
                GSON.fromJson(it, MessageMeta::class.java) 
            }
        )
        when (message.type) {
            MessageTypes.LOCAL_PLUGINS -> handleLocalPluginsQuery(message)
            MessageTypes.GET_SETTINGS -> handleGetSettingsQuery(message)
            MessageTypes.INSTALL_PLUGIN -> handleInstallPlugin(message)
            MessageTypes.UNINSTALL_PLUGIN -> handleUninstallPlugin(message)
            MessageTypes.UPDATE_PLUGIN -> handleUpdatePlugin(message)
            MessageTypes.TOGGLE_PLUGIN_BLACKLIST -> handleTogglePluginBlacklist(message)
            MessageTypes.SWITCH_PLUGIN_PLATFORM -> handleSwitchPluginPlatform(message)
            MessageTypes.UPDATE_SETTING -> handleUpdateSetting(message)
            else -> {
                PortalLogger.warn("Unknown message type: ${message.type}")
                sendError(message.id, message.type, ErrorCodes.INVALID_MESSAGE, "Unknown message type")
            }
        }
    }

    private fun handleSystemMessage(jsonObject: JsonObject) {
        val system = jsonObject.get("system").asString
        val data = jsonObject.get("data")?.asJsonObject
        
        when (system) {
            "connections-list" -> {
                val connections = data?.get("connections")?.asJsonObject
                val webCount = connections?.get("web")?.asInt ?: 0
                val pluginCount = connections?.get("plugin")?.asInt ?: 0
                if (webCount != lastLoggedWebCount || pluginCount != lastLoggedPluginCount) {
                    PortalLogger.info(PortalLogger.Action.WEBSOCKET, "Connected clients - Web: $webCount, Plugin: $pluginCount")
                    lastLoggedWebCount = webCount
                    lastLoggedPluginCount = pluginCount
                }
                
                // Update connected users count
                connectedUsers.clear()
                if (webCount > 0) {
                    connectedUsers.add("$webCount web user${if (webCount > 1) "s" else ""}")
                }
                
                // Emit as typed event
                emitConnectionStatus(ConnectionCounts(webCount, pluginCount))
            }
            "connection-joined" -> {
                // Request updated connections
                requestConnectionStatus()
            }
            "connection-left" -> {
                // Request updated connections
                requestConnectionStatus()
            }
            else -> {
                PortalLogger.warn("Unknown system message: $system")
            }
        }
    }

    private fun handleLocalPluginsQuery(message: WSMessage<JsonElement>) {
        try {
            val plugins = LocalPluginCache.toList().map { plugin ->
                TypedLocalPlugin(
                    platformId = plugin.platformId,
                    name = plugin.name,
                    version = plugin.version,
                    platform = plugin.platform.name,
                    sha256 = plugin.sha256,
                    sha512 = plugin.sha512,
                    installedAt = plugin.installedAt,
                    preferredChannel = plugin.preferredChannel,
                    excludedFromUpdates = plugin.excludedFromUpdates,
                )
            }

            sendSuccess(message.id, message.type, plugins)

        } catch (e: Exception) {
            PortalLogger.error("Error handling local plugins query: ${e.message ?: e::class.simpleName}")
            sendError(message.id, message.type, ErrorCodes.PLUGIN_CACHE_ERROR, "Failed to retrieve local plugins", mapOf("exception" to e.message))
        }
    }

    private fun handleGetSettingsQuery(message: WSMessage<JsonElement>) {
        try {
            val allSettings = Config.getAllSettings()
            val enabledFeatures = allSettings["EnabledFeatures"] as? Map<*, *>
            val polymartSettings = allSettings["Polymart"] as? Map<*, *>
            
            val settings = Settings(
                enabledFeatures = EnabledFeatures(
                    install = enabledFeatures?.get("INSTALL") as? Boolean ?: true,
                    update = enabledFeatures?.get("UPDATE") as? Boolean ?: true,
                    delete = enabledFeatures?.get("DELETE") as? Boolean ?: true,
                    list = enabledFeatures?.get("LIST") as? Boolean ?: true,
                    recognise = enabledFeatures?.get("RECOGNISE") as? Boolean ?: true,
                    import = enabledFeatures?.get("IMPORT") as? Boolean ?: true,
                    export = enabledFeatures?.get("EXPORT") as? Boolean ?: true,
                    automaticallyUpdatePpp = enabledFeatures?.get("AUTOMATICALLY_UPDATE_PPP") as? Boolean ?: false
                ),
                polymart = if (polymartSettings != null) {
                    PolymartSettings(
                        configured = polymartSettings["configured"] as? Boolean ?: false
                    )
                } else null
            )

            sendSuccess(message.id, message.type, settings)

        } catch (e: Exception) {
            PortalLogger.error("Error handling settings query: ${e.message ?: e::class.simpleName}")
            sendError(message.id, message.type, ErrorCodes.INTERNAL_ERROR, "Failed to retrieve settings")
        }
    }

    private fun handleInstallPlugin(message: WSMessage<JsonElement>) {
        try {
            val identifier = GSON.fromJson(message.payload, PluginIdentifier::class.java)
            
            // Validate platform
            if (!isValidPlatform(identifier.platform)) {
                sendError(message.id, message.type, ErrorCodes.INVALID_MESSAGE, "Invalid platform: ${identifier.platform}")
                return
            }
            
            val actionResponse = actions.install.run(JsonObject().apply {
                addProperty("platform", identifier.platform.lowercase())
                addProperty("id", identifier.id)
                identifier.version?.let { addProperty("version", it) }
                identifier.channel?.let { addProperty("channel", it) }
            })

            if (actionResponse.success) {
                sendSuccess(message.id, message.type, PluginOperationResult(
                    platform = identifier.platform,
                    id = identifier.id,
                    operation = "install"
                ))
            } else {
                sendError(message.id, message.type, ErrorCodes.INSTALL_FAILED, actionResponse.message ?: "Installation failed")
            }
        } catch (e: Exception) {
            PortalLogger.error("Error handling install: ${e.message ?: e::class.simpleName}")
            sendError(message.id, message.type, ErrorCodes.INSTALL_FAILED, "Failed to install plugin")
        }
    }

    private fun handleUninstallPlugin(message: WSMessage<JsonElement>) {
        try {
            val identifier = GSON.fromJson(message.payload, PluginIdentifier::class.java)
            
            // Validate platform
            if (!isValidPlatform(identifier.platform)) {
                sendError(message.id, message.type, ErrorCodes.INVALID_MESSAGE, "Invalid platform: ${identifier.platform}")
                return
            }
            
            val actionResponse = actions.uninstall.run(JsonObject().apply {
                addProperty("platform", identifier.platform.lowercase())
                addProperty("id", identifier.id)
            })

            if (actionResponse.success) {
                sendSuccess(message.id, message.type, PluginOperationResult(
                    platform = identifier.platform,
                    id = identifier.id,
                    operation = "uninstall"
                ))
            } else {
                sendError(message.id, message.type, ErrorCodes.UNINSTALL_FAILED, actionResponse.message ?: "Uninstallation failed")
            }
        } catch (e: Exception) {
            PortalLogger.error("Error handling uninstall: ${e.message ?: e::class.simpleName}")
            sendError(message.id, message.type, ErrorCodes.UNINSTALL_FAILED, "Failed to uninstall plugin")
        }
    }

    private fun handleUpdatePlugin(message: WSMessage<JsonElement>) {
        try {
            val identifier = GSON.fromJson(message.payload, PluginIdentifier::class.java)
            
            // Validate platform
            if (!isValidPlatform(identifier.platform)) {
                sendError(message.id, message.type, ErrorCodes.INVALID_MESSAGE, "Invalid platform: ${identifier.platform}")
                return
            }
            
            val actionResponse = actions.update.run(JsonObject().apply {
                addProperty("platform", identifier.platform.lowercase())
                addProperty("id", identifier.id)
            })

            if (actionResponse.success) {
                sendSuccess(message.id, message.type, PluginOperationResult(
                    platform = identifier.platform,
                    id = identifier.id,
                    operation = "update"
                ))
            } else {
                sendError(message.id, message.type, ErrorCodes.UPDATE_FAILED, actionResponse.message ?: "Update failed")
            }
        } catch (e: Exception) {
            PortalLogger.error("Error handling update: ${e.message ?: e::class.simpleName}")
            sendError(message.id, message.type, ErrorCodes.UPDATE_FAILED, "Failed to update plugin")
        }
    }

    private fun handleTogglePluginBlacklist(message: WSMessage<JsonElement>) {
        try {
            val identifier = GSON.fromJson(message.payload, PluginIdentifier::class.java)

            if (!isValidPlatform(identifier.platform)) {
                sendError(message.id, message.type, ErrorCodes.INVALID_MESSAGE, "Invalid platform: ${identifier.platform}")
                return
            }

            val actionResponse = actions.toggleBlacklist.run(JsonObject().apply {
                addProperty("platform", identifier.platform.lowercase())
                addProperty("id", identifier.id)
            })

            if (actionResponse.success) {
                sendSuccess(message.id, message.type, PluginOperationResult(
                    platform = identifier.platform,
                    id = identifier.id,
                    operation = "blacklist"
                ))
            } else {
                sendError(message.id, message.type, ErrorCodes.BLACKLIST_FAILED, actionResponse.message ?: "Blacklist update failed")
            }
        } catch (e: Exception) {
            PortalLogger.error("Error handling blacklist toggle: ${e.message ?: e::class.simpleName}")
            sendError(message.id, message.type, ErrorCodes.BLACKLIST_FAILED, "Failed to update blacklist")
        }
    }

    private fun handleSwitchPluginPlatform(message: WSMessage<JsonElement>) {
        try {
            val identifier = GSON.fromJson(message.payload, PluginIdentifier::class.java)
            val targetPlatform = identifier.targetPlatform

            if (!isValidPlatform(identifier.platform)) {
                sendError(message.id, message.type, ErrorCodes.INVALID_MESSAGE, "Invalid platform: ${identifier.platform}")
                return
            }

            if (targetPlatform == null || !isValidPlatform(targetPlatform)) {
                sendError(message.id, message.type, ErrorCodes.INVALID_MESSAGE, "Invalid target platform: $targetPlatform")
                return
            }

            val actionResponse = actions.switchPlatform.run(JsonObject().apply {
                addProperty("platform", identifier.platform.lowercase())
                addProperty("id", identifier.id)
                addProperty("targetPlatform", targetPlatform.lowercase())
            })

            if (actionResponse.success) {
                sendSuccess(message.id, message.type, PluginOperationResult(
                    platform = targetPlatform,
                    id = identifier.id,
                    operation = "platform"
                ))
            } else {
                sendError(message.id, message.type, ErrorCodes.PLATFORM_SWITCH_FAILED, actionResponse.message ?: "Platform switch failed")
            }
        } catch (e: Exception) {
            PortalLogger.error("Error handling platform switch: ${e.message ?: e::class.simpleName}")
            sendError(message.id, message.type, ErrorCodes.PLATFORM_SWITCH_FAILED, "Failed to switch platform")
        }
    }


    private fun handleUpdateSetting(message: WSMessage<JsonElement>) {
        try {
            val data = message.payload.asJsonObject
            val path = data.get("path")?.asString
            val valueElement = data.get("value")

            if (path.isNullOrBlank()) {
                sendError(message.id, message.type, ErrorCodes.INVALID_SETTING_PATH, "Invalid or missing setting path")
                return
            }

            val value: Any? = when {
                valueElement == null || valueElement.isJsonNull -> null
                valueElement.isJsonPrimitive -> {
                    val primitive = valueElement.asJsonPrimitive
                    when {
                        primitive.isBoolean -> primitive.asBoolean
                        primitive.isNumber -> primitive.asNumber
                        primitive.isString -> primitive.asString
                        else -> primitive.asString
                    }
                }
                else -> valueElement.toString()
            }

            if (!Config.validateSetting(path, value)) {
                sendError(message.id, message.type, ErrorCodes.INVALID_SETTING_PATH, "Invalid value for setting: $path")
                return
            }

            val success = Config.updateSetting(path, value)

            if (success) {
                sendSuccess(message.id, message.type, mapOf("path" to path, "newValue" to value))
                PortalLogger.info(PortalLogger.Action.WEBSOCKET, "Updated setting $path to $value")
            } else {
                sendError(message.id, message.type, ErrorCodes.INTERNAL_ERROR, "Failed to update setting")
            }

        } catch (e: Exception) {
            PortalLogger.error("Error updating setting: ${e.message ?: e::class.simpleName}")
            sendError(message.id, message.type, ErrorCodes.INTERNAL_ERROR, "Failed to update setting")
        }
    }

    private fun sendSuccess(id: String, type: String, data: Any) {
        updateActivity()
        val response = WSResponse(
            id = id,
            type = type,
            result = WSResult.Success(data = data)
        )
        val json = wsGson.toJson(response)
        client?.send(json)
    }

    private fun sendError(id: String, type: String, code: String, message: String, details: Map<String, Any?>? = null) {
        updateActivity()
        val response = WSResponse<Any>(
            id = id,
            type = type,
            result = WSResult.Error(
                error = WSError(
                    code = code,
                    message = message,
                    details = details?.mapValues { JsonPrimitive(it.value?.toString()) }
                )
            )
        )
        client?.send(wsGson.toJson(response))
    }

    private fun emitConnectionStatus(connections: ConnectionCounts) {
        val event = WSMessage(
            id = generateId(),
            type = MessageTypes.CONNECTION_STATUS,
            payload = ConnectionStatus(connections = connections),
            meta = MessageMeta(timestamp = System.currentTimeMillis())
        )
        client?.send(GSON.toJson(event))
    }

    private fun requestConnectionStatus() {
        // System message to request connection status
        client?.send("""{"system":"connections-request"}""")
    }

    private fun scheduleReconnect() {
        if (!isEnabled) return
        if (reconnectExecutor.isShutdown || reconnectExecutor.isTerminated) return
        if (reconnectScheduled) return

        reconnectScheduled = true

        reconnectExecutor.schedule({
            if (isEnabled) {
                reconnectScheduled = false
                PortalLogger.info(PortalLogger.Action.WEBSOCKET, "Attempting to reconnect to websocket...")
                connectToWebSocket()
            }
        }, 5, TimeUnit.SECONDS)
    }

    private fun shouldReconnectAfterClose(code: Int): Boolean {
        val isTemporaryRoom = this::roomCode.isInitialized && roomCode != "server"
        if (isTemporaryRoom && code == 4001) {
            isEnabled = false
            PortalLogger.warn("Temporary editor websocket was rejected or expired; reconnect disabled. Run /pp editor to create a new session.")
            return false
        }

        return true
    }

    private fun generateId(): String {
        return "${System.currentTimeMillis()}_${(0..999999).random()}"
    }

    private fun isValidPlatform(platform: String): Boolean {
        return platform in listOf("MODRINTH", "SPIGOTMC", "HANGAR", "POLYMART")
    }
}
