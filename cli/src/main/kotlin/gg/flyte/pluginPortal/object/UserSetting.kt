package gg.flyte.pluginPortal.`object`

enum class UserSetting(val displayName: String, val description: String, val variableName: String, val type: Class<*>? = Boolean::class.java) {
    BROWSER_PREVIEW("Browser Preview", "Opens the plugin page in your browser when true", "browserPreview"),
    AUTO_UPDATE("Auto Update", "Automatically updates plugins when true", "autoUpdatePlugins"),
    AUTO_RESTART("Auto Restart", "Automatically restarts the server when true", "autoRestart"),
    DEFAULT_OPERATORS("Default Operators", "Automatically op players when true upon creating a server", "defaultOperators", List::class.java),
    AUTO_ACCEPT_EULA("Auto Accept EULA", "Automatically accepts the EULA when true", "autoAcceptEula"),

}