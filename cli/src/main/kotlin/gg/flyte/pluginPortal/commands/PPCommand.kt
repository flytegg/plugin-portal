package gg.flyte.pluginPortal.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand

class PPCommand : NoOpCliktCommand(
    name = "pluginportal",
    help = "Utilize PluginPortal to install plugins to your server",
) {
    override fun run() = Unit

    override fun aliases(): Map<String, List<String>> = mapOf(
        "s" to listOf("server"),
        "p" to listOf("plugins"),
        "set" to listOf("settings"),
    )
}

class ServerCommand : NoOpCliktCommand(
    name = "server",
    help = "Manage the Server"
) {
    override fun run() = Unit

    override fun aliases(): Map<String, List<String>> = mapOf(
        "st" to listOf("start"),
        "c" to listOf("create"),
        "d" to listOf("delete"),
        "l" to listOf("list"),
        "sl" to listOf("select"),
        "pr" to listOf("preset"),
        "i" to listOf("info"),
    )
}

class PresetCommand : NoOpCliktCommand(
    name = "preset",
    help = "Manage the server presets"
) {
    override fun run() = Unit

    override fun aliases(): Map<String, List<String>> = mapOf(
        "ls" to listOf("list"),
        "s" to listOf("save"),
        "l" to listOf("load"),
        "d" to listOf("delete"),
    )
}

class Plugins : NoOpCliktCommand(
    name = "plugins",
    help = "Manage Server's Plugins"
) {
    override fun run() = Unit

    override fun aliases(): Map<String, List<String>> = mapOf(
        "i" to listOf("install"),
        "p" to listOf("preview"),
        "u" to listOf("update"),
        "ls" to listOf("list"),
        "d" to listOf("delete"),
        "r" to listOf("request"),
        "s" to listOf("search"),

    )
}