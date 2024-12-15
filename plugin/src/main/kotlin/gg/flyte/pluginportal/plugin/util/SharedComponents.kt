package gg.flyte.pluginportal.plugin.util

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.chat.appendDark
import gg.flyte.pluginportal.plugin.chat.showOnHover
import gg.flyte.pluginportal.plugin.chat.suggestCommand
import gg.flyte.pluginportal.plugin.chat.textDark
import gg.flyte.pluginportal.plugin.manager.MarketplacePluginCache.isUpToDate
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor

object SharedComponents {

    val DISCORD_COMPONENT = text("Discord", NamedTextColor.AQUA)
        .showOnHover("Click here to join our Discord", NamedTextColor.AQUA)
        .clickEvent(ClickEvent.openUrl("https://discord.gg/flyte"))

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

    fun getInstallButton(plugin: Plugin, installed: Boolean) =
        getInstallButton(plugin.name, plugin.platforms[plugin.highestPriorityPlatform]!!.id, plugin.highestPriorityPlatform, installed)

    fun getUpdateButton(plugin: LocalPlugin) = if (!plugin.isUpToDate) button("Update",
        "Click here to update ${plugin.name}",
        "/pp update \"${plugin.platformId}\" --byId",
        NamedTextColor.AQUA) else getUpToDateButton(plugin)

    private fun getUpToDateButton(plugin: LocalPlugin) = button(
        "Up to date",
        "This plugin is already up to date. Click here to force an update anyway.",
        "/pp update \"${plugin.platformId}\" --byId --ignoreOutdated",
        NamedTextColor.GRAY)

    private fun button(buttonName: String, hoverText: String, clickSuggest: String, color: NamedTextColor) =
        textDark("[").append(text(buttonName, color)).appendDark("]")
            .showOnHover(hoverText, color)
            .suggestCommand(clickSuggest)

}