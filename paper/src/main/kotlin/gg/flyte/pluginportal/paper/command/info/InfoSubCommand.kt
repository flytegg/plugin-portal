package gg.flyte.pluginportal.paper.command.info

import gg.flyte.pluginportal.paper.PluginPortal
import gg.flyte.pluginportal.paper.command.CommandManager
import gg.flyte.pluginportal.paper.command.info.display.InfoDisplay
import gg.flyte.pluginportal.paper.config.language.sendError
import gg.flyte.pluginportal.paper.config.language.sendInfo
import io.papermc.lib.PaperLib.isPaper
import io.papermc.lib.PaperLib.isSpigot
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class InfoSubCommand {

    @Subcommand("info")
    @CommandPermission("pluginportal.command.info")
    @AutoComplete("@marketplacePlugin")
    fun infoSubCommand(
        sender: Audience,
        @CommandManager.PPPlugin @Named("pluginName") @Optional pluginName: String? = null,
        @Flag @Optional @Default("false") isId: Boolean,
    ) {
        if (pluginName == null) {

            return sender.sendInfo(
                """Usage: /pp info <plugin>
 - Plugin Portal Version: ${PluginPortal.instance.description.version}
 - Native Support: ${!isSpigot()}
 - Outdated: ${"TODO"}
                         """
            )
        }

        val plugins = CommandManager.getPlugins(pluginName, isId).also {
            if (it.isEmpty()) return sender.sendError("No plugins found with the name '$pluginName'")
        }

        if (plugins.size > 1) {
            plugins.forEach { plugin ->
                sender.sendInfo(" - ${plugin.getUniqueName()} | ${plugin.releaseData.latestVersion}", false)
            }
        } else {
            val plugin = plugins.first()

            sender.sendMessage(InfoDisplay.DefaultDisplay().getDisplayInfo(plugin))
        }
    }
}