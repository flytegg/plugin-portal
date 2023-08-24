package gg.flyte.pluginPortal.`object`.serializer

data class SerializedConfig(
    var browserPreview: Boolean = true,
    var autoUpdatePlugins: Boolean = false,
    var autoRestart: Boolean = false,
    var autoAcceptEula: Boolean = true,
    val selectServerUponCreation: Boolean = true,
    val debug: Boolean = false,
    var activeServerName: String? = null,
    var lastLaunchedVersion: String? = "2.0.0",
    val defaultOperators: ArrayList<String> = arrayListOf(),
    val installedPlugins: HashSet<String> = hashSetOf() // Plugin id's
)
