package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class ListSubCommand {

    @Subcommand("list",)
    @AutoComplete("@marketplacePluginSearch *")
    @CommandPermission("pluginportal.view")
    fun listCommand(audience: Audience) {
        val plugins = LocalPluginCache
            .sortedBy { plugin -> plugin.name }

        if (plugins.isEmpty()) return audience.sendMessage(text("No plugins found", NamedTextColor.GRAY).boxed())

        var message = text("Plugins installed with Plugin Portal", NamedTextColor.GRAY)

        plugins.forEach { plugin ->
            message = message.append(text("\n"))
                .append(text(" - ", NamedTextColor.DARK_GRAY))
                .append(textPrimary(plugin.name)
                    .showOnHover("Click to view this plugin")
                    .suggestCommand("/pp view ${plugin.name}")
                )
                .append(textDark(" (${plugin.platform.name}) "))
                .append(SharedComponents.getInstallButton(plugin.name, true))
        }

        audience.sendMessage(message.boxed())
    }

}