package gg.flyte.pluginPortal.`object`.serializer

data class SerializedConfig(
    var browserPreview: Boolean = true,
    var autoUpdatePlugins: Boolean = false,
    var autoRestart: Boolean = false,
    var autoAcceptEula: Boolean = true,
    val selectServerUponCreation: Boolean = true,
    val debug: Boolean = false,
    val defaultOperators: ArrayList<String> = arrayListOf()
)
