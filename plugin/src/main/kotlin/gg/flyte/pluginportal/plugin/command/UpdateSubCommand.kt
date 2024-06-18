package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache.findFile
import gg.flyte.pluginportal.plugin.util.*
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand

@Command("pp", "pluginportal", "ppm")
class UpdateSubCommand {

    @Subcommand("update")
    @AutoComplete("@installedPluginSearch *")
    fun updateCommand(
        audience: Audience,
        name: String
    ) {
        val plugin = LocalPluginCache.filter { it.name == name }

        if (plugin.isEmpty()) return audience.sendMessage(status(Status.FAILURE, "Plugin not found").boxed())
        if (plugin.size > 1) return audience.sendMessage(status(Status.FAILURE, "Multiple plugins found").boxed())

        audience.sendMessage(textSecondary("Updating ur plugin...").boxed())
    }
}