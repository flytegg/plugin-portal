package gg.flyte.pluginportal.bukkit.command.downloadable

import gg.flyte.pluginportal.bukkit.PluginPortal
import gg.flyte.pluginportal.bukkit.command.CommandManager
import gg.flyte.pluginportal.bukkit.manager.language.sendError
import gg.flyte.pluginportal.bukkit.manager.language.sendInfo
import gg.flyte.pluginportal.bukkit.manager.language.sendSuccess
import gg.flyte.pluginportal.bukkit.manager.PPPluginCache.isInstalled
import gg.flyte.pluginportal.bukkit.manager.PluginManager
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Default
import revxrsal.commands.annotation.Flag
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class InstallSubCommand {

    @Subcommand("install", "i")
    @CommandPermission("pluginportal.install")
    @AutoComplete("@marketplacePlugin")
    fun installSubCommand(
        sender: Audience,
        @CommandManager.PPPlugin pluginName: String,
        @Flag @Optional @Default("false") isId: Boolean,
    ) {
        val plugins = CommandManager.getPlugins(pluginName, isId).also {
            if (it.isEmpty()) return sender.sendError("No plugins found with the name '$pluginName'")
        }

        if (plugins.size > 1) {
            plugins.forEach {
                sender.sendInfo(" - ${it.getUniqueName()} | ${it.getLatestVersion()?.name}", false)
            }
        } else {
            val plugin = plugins.first()

            if (plugin.isInstalled()) return sender.sendError("Plugin ${plugin.getUniqueName()} is already installed. Please use the update command.")

            if (plugin.getLatestVersion()?.downloadUrl == null)
                return sender.sendError("No download URL found for ${plugin.displayInfo.name}")

            sender.sendInfo("Downloading ${plugin.getUniqueName()}")

            PluginPortal.instance.asyncDispatch {
                PluginManager.installPlugin(plugin) { success ->
                    if (success) sender.sendSuccess("Successfully installed ${plugin.getUniqueName()}")
                    else sender.sendError("Failed to install ${plugin.getUniqueName()}")
                }
            }
        }
    }

}