package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.util.appendPrimary
import gg.flyte.pluginportal.plugin.util.boxed
import gg.flyte.pluginportal.plugin.util.textDark
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand

@Command("pp", "pluginportal", "ppm")
class ListSubCommand {

    @Subcommand("list",)
    @AutoComplete("@marketplacePluginSearch *")
    fun listCommand(audience: Audience) {
        val plugins = LocalPluginCache
            .sortedBy { plugin -> plugin.name }

        if (plugins.isEmpty()) return audience.sendMessage(text("No plugins found", NamedTextColor.GRAY).boxed())

        var message = text("Plugins installed with Plugin Portal", NamedTextColor.GRAY)

        plugins.forEach { plugin ->
            message = message.append(text("\n"))
                .append(text(" - ", NamedTextColor.DARK_GRAY))
                .appendPrimary(plugin.name)
                .append(textDark(" (${plugin.platform.name})"))
        }

        audience.sendMessage(message.boxed())
    }

}