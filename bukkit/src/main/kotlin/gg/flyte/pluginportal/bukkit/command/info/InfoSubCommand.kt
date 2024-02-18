package gg.flyte.pluginportal.bukkit.command.info

import gg.flyte.pluginportal.bukkit.PluginPortal
import gg.flyte.pluginportal.bukkit.command.CommandManager
import gg.flyte.pluginportal.bukkit.command.info.display.InfoDisplay
import gg.flyte.pluginportal.bukkit.manager.language.sendError
import gg.flyte.pluginportal.bukkit.manager.language.sendInfo
import io.papermc.lib.PaperLib.isPaper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience
import org.bukkit.command.CommandSender
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission
import java.util.stream.Stream

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
 - Native Support: ${isPaper()}
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