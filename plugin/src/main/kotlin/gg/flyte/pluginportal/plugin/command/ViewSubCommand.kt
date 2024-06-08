package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.plugin.PluginPortal.Companion.GSON
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.MiniMessage
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand

@Command("pp", "pluginportal", "ppm")

class ViewSubCommand {

    @Subcommand("view")
    @AutoComplete("@plugin")
    fun viewCommand(audience: Audience, prefix: String) {
        val plugins = API.getPlugins(prefix)

        plugins.forEach { plugin ->
            audience.sendMessage(
                text(
                    plugin.name + plugin.platforms.keys.joinToString(
                        prefix = " (",
                        postfix = ")",
                        separator = ", "
                    )
                )
            )}
    }
}