package gg.flyte.pluginPortal.commands

import com.github.ajalt.clikt.core.CliktCommand
import java.awt.Desktop
import java.net.URI




class UpdatePlugin : CliktCommand(
    name = "update",
    help = "Update an installed plugin to the latest version."
) {
    override fun run() = echo("managing update plugins")
}


