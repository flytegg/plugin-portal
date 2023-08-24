package gg.flyte.pluginPortal.commands

import com.github.ajalt.clikt.core.CliktCommand

class PPCommand : CliktCommand(
    name = "pluginportal",
    help = "Utilize PluginPortal to install plugins to your server",
) {
    override fun run() = Unit
}