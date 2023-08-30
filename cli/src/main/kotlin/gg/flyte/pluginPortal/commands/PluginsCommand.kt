package gg.flyte.pluginPortal.commands

import com.github.ajalt.clikt.core.CliktCommand
import java.awt.Desktop
import java.net.URI

class Plugins : CliktCommand(
    name = "plugins",
    help = "Manage Server's Plugins"
) {
    override fun run() = Unit
}





class UpdatePlugin : CliktCommand(
    name = "update",
    help = "Update an installed plugin to the latest version."
) {
    override fun run() = echo("managing update plugins")
}

class DeletePlugin : CliktCommand(
    name = "delete",
    help = "Remove a plugin from the server."
) {
    override fun run() = echo("managing delete plugins")
}



