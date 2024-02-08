package gg.flyte.pluginportal.bukkit.command.info

import gg.flyte.pluginportal.bukkit.manager.PPPluginCache
import gg.flyte.pluginportal.bukkit.manager.language.Message.solidLine
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class ListSubCommand {

    @Subcommand("list", "ls")
    @CommandPermission("pluginportal.command.list")
    fun listSubCommand(sender: Audience) {
        val strike = Component.text("")
            .solidLine()
            .color(NamedTextColor.DARK_GRAY)

        println(PPPluginCache.getInstalledPlugins())
        println(PPPluginCache.getInstalledPlugins().isEmpty())

        sender.sendMessage(
            Component.text().append(
                strike,
                Component.newline(),
                if (PPPluginCache.getInstalledPlugins().isEmpty()) {
                    Component.text("No plugins installed.", NamedTextColor.RED).also {
                        println("No plugins installed. Appending")
                    }
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