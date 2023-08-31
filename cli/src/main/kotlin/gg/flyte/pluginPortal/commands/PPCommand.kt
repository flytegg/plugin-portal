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
        "pr" to listOf("preset"),
        "set" to listOf("settings"),
    )
}