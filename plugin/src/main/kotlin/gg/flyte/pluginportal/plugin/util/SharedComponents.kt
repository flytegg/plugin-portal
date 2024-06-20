package gg.flyte.pluginportal.plugin.util

import gg.flyte.pluginportal.plugin.chat.appendDark
import gg.flyte.pluginportal.plugin.chat.showOnHover
import gg.flyte.pluginportal.plugin.chat.suggestCommand
import gg.flyte.pluginportal.plugin.chat.textDark
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor

object SharedComponents {

    fun getInstallButton(name: String, installed: Boolean): Component {
        val install = if (installed) "Uninstall" else "Install"
        val color = if (installed) NamedTextColor.RED else NamedTextColor.AQUA

        return button(install.capitaliseFirst(), "", "/pp $install $name", color)
            .showOnHover("Click here to $install $name", color)
    }

    // TODO: Grey out button if is up to date, but only if this info is already in the cache
    fun getUpdateButton(name: String) = button(
        "Update",
        "Click here to update $name",
        "/pp update $name",
        NamedTextColor.AQUA)

    private fun button(buttonName: String, hoverText: String, clickSuggest: String, color: NamedTextColor) =
        textDark("[").append(text(buttonName, color)).appendDark("]")
            .showOnHover(hoverText, NamedTextColor.AQUA)
            .suggestCommand(clickSuggest)

}