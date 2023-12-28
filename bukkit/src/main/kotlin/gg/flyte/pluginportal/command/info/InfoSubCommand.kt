package gg.flyte.pluginportal.command.info

import gg.flyte.pluginportal.command.CommandManager
import gg.flyte.pluginportal.command.info.display.InfoDisplay
import gg.flyte.pluginportal.manager.language.sendError
import gg.flyte.pluginportal.manager.language.sendInfo
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class InfoSubCommand {

    @Subcommand("info")
    @CommandPermission("pluginportal.info")
    @AutoComplete("@marketplacePlugin")
    fun infoSubCommand(
        sender: Audience,
        @CommandManager.PPPlugin pluginName: String,
        @Flag @Optional @Default("false") isId: Boolean,
    ) {
        val plugins = CommandManager.getPlugins(pluginName, isId).also {
            if (it.isEmpty()) return sender.sendError("No plugins found with the name '$pluginName'")
        }

        if (plugins.size > 1) {
            plugins.forEach { plugin ->
                sender.sendInfo(" - ${plugin.getUniqueName()} | ${plugin.versionData.latestVersion}", false)
            }
        } else {
            val plugin = plugins.first()

            sender.sendMessage(InfoDisplay.DefaultDisplay().getDisplayInfo(plugin))
        }
    }

}