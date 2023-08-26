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

class PreviewPlugin : CliktCommand(
    name = "preview",
    help = "Show plugin information for correct installation."
) {
    override fun run() {

        if (true) {
            echo("Opening Plugin Page in browser...")
            Desktop.getDesktop().browse(URI("https://www.spigotmc.org/resources/9089"))
            echo("To disable this feature, please visit the config.")
        } else {
            echo("Plugin info: ")
        }

    }
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



