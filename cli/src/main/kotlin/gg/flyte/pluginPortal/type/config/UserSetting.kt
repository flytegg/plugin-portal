package gg.flyte.pluginPortal.type.config

/**
 *     var browserPreview: Boolean = true,
 *     var autoUpdatePlugins: Boolean = false,
 *     var autoRestart: Boolean = false,
 *     var autoAcceptEula: Boolean = true,
 *     val selectServerUponCreation: Boolean = true,
 *     val debug: Boolean = false,
 *     var activeServerName: String? = null,
 *     var lastLaunchedVersion: String? = "2.0.0",
 *     val defaultOperators: ArrayList<String> = arrayListOf(),
 */
enum class UserSetting(val displayName: String, val description: String, val variableName: String, val type: Class<*>? = Boolean::class.java) {
    BROWSER_PREVIEW("Browser Preview", "Opens the plugin page in your browser when true", "browserPreview"),
    AUTO_UPDATE("Auto Update", "Automatically updates plugins when true", "autoUpdatePlugins"),
    AUTO_RESTART("Auto Restart", "Automatically restarts the server when true", "autoRestart"),
    AUTO_ACCEPT_EULA("Auto Accept EULA", "Automatically accepts the EULA when true", "autoAcceptEula"),
    SELECT_SERVER_UPON_CREATION("Select Server Upon Creation", "Automatically select the server upon creation when true", "selectServerUponCreation"),
    DEFAULT_OPERATORS("Default Operators", "Automatically op players when true upon creating a server", "defaultOperators", List::class.java),
    DEBUG("Debug", "Enables debug mode when true, This shows URL requests", "debug"),
}