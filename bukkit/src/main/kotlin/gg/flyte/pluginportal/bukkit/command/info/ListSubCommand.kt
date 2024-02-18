package gg.flyte.pluginportal.bukkit.command.info

import gg.flyte.pluginportal.bukkit.manager.PPPluginCache
import gg.flyte.pluginportal.bukkit.manager.language.Message.solidLine
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer

@CommandContainer
class ListSubCommand {

    @Command("pluginportal|pp|ppm list|ls")
    @Permission("pluginportal.command.list")
    fun onListSubCommand(sender: Audience) {
        val strike = Component.text("")
            .solidLine()
            .color(NamedTextColor.DARK_GRAY)

        sender.sendMessage(
            Component.text().append(
                strike,
                Component.newline(),
                if (PPPluginCache.getInstalledPlugins().isEmpty()) {
                    Component.text("No plugins installed.", NamedTextColor.RED)
                    Component.newline()
                } else {
                    Component.text().append(
                        PPPluginCache.getInstalledPlugins().map { plugin ->
                            Component.text().append(
                                Component.text(" - ", NamedTextColor.GRAY),
                                Component.text(plugin.getUniqueName(), NamedTextColor.AQUA),
                                Component.text(" | ", NamedTextColor.GRAY),
                                Component.text(plugin.version ?: "Unknown Version", NamedTextColor.AQUA),
                                Component.newline()
                            ).build()
                        }
                    ).build()
                },
                strike
            )
        )

    }

}