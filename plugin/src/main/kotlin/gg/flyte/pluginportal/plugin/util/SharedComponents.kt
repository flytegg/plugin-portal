package gg.flyte.pluginportal.plugin.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor

object SharedComponents {

    fun getInstallButton(name: String, installed: Boolean): Component {
        val install = if (installed) "uninstall" else "install"
        val colour = if (installed) NamedTextColor.RED else NamedTextColor.AQUA

        return button(install.capitaliseFirst(), "", "/pp $install $name", colour)
            .showOnHover("Click here to $install $name", colour)
    }

    // TODO: Grey out button if is up to date, but only if this info is already in the cache
    fun getUpdateButton(name: String) = button(
        "Update",
        "Click here to update $name",
        "/pp update $name",
        NamedTextColor.AQUA)

    private fun button(buttonName: String, hoverText: String, clickSuggest: String, colour: NamedTextColor) =
        textDark("[").append(text(buttonName, colour)).appendDark("]")
            .showOnHover(hoverText, NamedTextColor.AQUA)
            .suggestCommand(clickSuggest)

}