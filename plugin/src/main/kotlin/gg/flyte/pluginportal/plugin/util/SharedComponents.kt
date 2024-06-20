package gg.flyte.pluginportal.plugin.util

import gg.flyte.pluginportal.plugin.chat.appendDark
import gg.flyte.pluginportal.plugin.chat.suggestCommand
import gg.flyte.pluginportal.plugin.chat.textDark
import gg.flyte.pluginportal.plugin.chat.textPrimary
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor

object SharedComponents {

    fun getInstallButton(name: String, installed: Boolean): Component {
        val install = if (installed) "uninstall" else "install"
        val colour = if (installed) NamedTextColor.RED else NamedTextColor.AQUA

        return textDark("[").append(text(install.capitaliseFirst(), colour)).appendDark("]")
                .hoverEvent(HoverEvent.showText(textPrimary("Click to $install $name")))
                .suggestCommand("/pp $install $name")
    }

}