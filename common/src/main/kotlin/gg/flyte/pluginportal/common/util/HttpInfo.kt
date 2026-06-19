package gg.flyte.pluginportal.common.util

object HttpInfo {
    private const val API_DEV_URL = "http://localhost:3001"
    private const val API_PROD_URL = "https://v3.pluginportal.link"
    private const val SOCKET_DEV_URL = "ws://localhost:3001"
    private const val SOCKET_PROD_URL = "wss://v3.pluginportal.link"

    fun getSocketBaseUrl(): String {
        return if (isDevelopment()) SOCKET_DEV_URL else SOCKET_PROD_URL
    }
    
    fun getApiBaseUrl(): String {
        return if (isDevelopment()) API_DEV_URL else API_PROD_URL
    }
    
    private fun isDevelopment(): Boolean {
        return System.getProperty("pluginportal.dev", "false").toBoolean()
    }
}
