package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.util.Status
import gg.flyte.pluginportal.plugin.util.boxed
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



    }
}