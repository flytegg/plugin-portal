package gg.flyte.pluginportal.common.util

object HttpInfo {
    private const val API_DEV_URL = "http://localhost:3001"
    private const val API_PROD_URL = "https://v3.pluginportal.link"
    private const val SOCKET_PROD_URL = "wss://v3.pluginportal.link"

    fun getSocketBaseUrl(): String {
        return if (isDevelopment()) getApiBaseUrl().replaceFirst("https://", "wss://").replaceFirst("http://", "ws://") else SOCKET_PROD_URL
    }
    
    fun getApiBaseUrl(): String {
        return if (isDevelopment()) {
            System.getProperty("pluginportal.apiUrl", API_DEV_URL).also {
                val uri = java.net.URI(it)
                require(uri.scheme in setOf("http", "https") && uri.userInfo == null && uri.query == null && uri.fragment == null) { "Invalid development API URL" }
            }
        } else API_PROD_URL
    }
    
    private fun isDevelopment(): Boolean {
        return System.getProperty("pluginportal.dev", "false").toBoolean()
    }
}
