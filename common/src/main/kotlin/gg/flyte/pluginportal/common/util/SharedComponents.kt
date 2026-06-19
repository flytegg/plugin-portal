package gg.flyte.pluginportal.common.util

import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor

object SharedComponents {

    val DISCORD_COMPONENT = Component.text("Discord", NamedTextColor.AQUA)
        .showOnHover("Click here to join our Discord", NamedTextColor.AQUA)
        .clickEvent(ClickEvent.openUrl("https://flyte.gg/discord"))
        .hyperlink()

    fun successfullyInstalledPlugin(pluginName: String, platform: MarketplacePlatform, plsrestart: Boolean = true) =
        status(Status.SUCCESS, "Downloaded $pluginName from ${platform.name}.")
            .also { if (plsrestart) it.appendSecondary("\n- Please restart your server to enable this plugin") }
            .append(endLine())

    fun getUpdateBeforeAndAfterComponent(plugin: LocalPlugin, new: Plugin) = Component.text(
        "(",
        NamedTextColor.DARK_GRAY
    )
        .append(Component.text(plugin.version, NamedTextColor.RED))
        .append(Component.text(" → ", NamedTextColor.DARK_GRAY))
        .append(Component.text(plugin.targetUpdateVersion(new)?.versionNumber ?: "", NamedTextColor.GREEN))
        .append(Component.text(")", NamedTextColor.DARK_GRAY))

    private fun getInstallButton(name: String, id: String, platform: MarketplacePlatform, installed: Boolean): Component {
        val install = if (installed) "uninstall" else "install"
        val color = if (installed) NamedTextColor.RED else NamedTextColor.AQUA

        return button(install.capitaliseFirst(),
            "",
            "/pp $install \"$id\"${if (installed) "" else " $platform"} --byId", // no platform for uninstall
            color)
            .showOnHover("Click here to $install $name", color)
    }

    fun getInstallButton(plugin: LocalPlugin, installed: Boolean) =
        getInstallButton(plugin.name, plugin.platformId, plugin.platform, installed)

    fun getInstallButton(plugin: Plugin, installed: Boolean): Component {
        val platform = plugin.platforms.bestDownloadable
            ?: return textDark("[")
                .append(Component.text("Unavailable", NamedTextColor.GRAY))
                .appendDark("]")
                .showOnHover("No enabled downloadable platform is available for ${plugin.name}", NamedTextColor.GRAY)

        return getInstallButton(plugin.name, platform.platformId, platform.platform, installed)
    }

    fun getUpdateButton(plugin: LocalPlugin): TextComponent {
        if (plugin.isUpToDate) return getUpToDateButton(plugin)
        val marketplace = MarketplacePluginCache.getCachedPluginById(plugin.platform, plugin.platformId) ?: return getUpToDateButton(plugin)
        return button("Update",
            getUpdateBeforeAndAfterComponent(plugin, marketplace).append(
                Component.text(
                    "\nClick here to update ${plugin.name}",
                    NamedTextColor.GRAY
                )
            ),
            "/pp update \"${plugin.platformId}\" --byId",
            NamedTextColor.AQUA)
    }

    private fun getUpToDateButton(plugin: LocalPlugin) = button(
        "Up to date",
        Component.text("(", NamedTextColor.DARK_GRAY)
            .append(Component.text(plugin.version, NamedTextColor.GREEN))
            .append(Component.text(")", NamedTextColor.DARK_GRAY))
            .append(
                Component.text(
                    "\nThis plugin is already up to date. Click here to force an update anyway.",
                    NamedTextColor.GRAY
                )
            ),
        "/pp update \"${plugin.platformId}\" --byId --ignoreOutdated",
        NamedTextColor.GRAY)

    private fun button(buttonName: String, hoverText: TextComponent, clickSuggest: String, color: NamedTextColor) = textDark("[")
        .append(Component.text(buttonName, color)).appendDark("]")
        .hoverEvent(HoverEvent.showText(Component.text("", color).append(hoverText)))
        .suggestCommand(clickSuggest)

    private fun button(buttonName: String, hoverText: String, clickSuggest: String, color: NamedTextColor) =
        button(buttonName, Component.text(hoverText, color), clickSuggest, color)
}
