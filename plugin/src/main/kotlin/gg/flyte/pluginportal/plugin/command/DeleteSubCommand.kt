package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache.findFile
import gg.flyte.pluginportal.plugin.util.Status
import gg.flyte.pluginportal.plugin.util.boxed
import gg.flyte.pluginportal.plugin.util.endLine
import gg.flyte.pluginportal.plugin.util.status
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand

@Command("pp", "pluginportal", "ppm")
class DeleteSubCommand {

    @Subcommand("delete")
    @AutoComplete("@installedPluginSearch *")
    fun deleteCommand(
        audience: Audience,
        name: String
    ) {
        val plugin = LocalPluginCache.filter { it.name == name }

        if (plugin.isEmpty()) return audience.sendMessage(status(Status.FAILURE, "Plugin not found").boxed())
        if (plugin.size > 1) return audience.sendMessage(status(Status.FAILURE, "Multiple plugins found").boxed())

        val file = plugin.first().findFile()
        if (file == null) {
            LocalPluginCache.deletePlugin(plugin.first())
            return audience.sendMessage(status(Status.FAILURE, "Plugin file not found").boxed())
        }

        LocalPluginCache.deletePlugin(plugin.first())

        audience.sendMessage(status(Status.SUCCESS, "Plugin deleted").append(endLine()))
    }
}