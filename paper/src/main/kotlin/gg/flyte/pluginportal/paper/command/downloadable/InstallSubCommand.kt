package gg.flyte.pluginportal.paper.command.downloadable

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import gg.flyte.pluginportal.paper.PluginPortal
import gg.flyte.pluginportal.paper.command.CommandManager
import gg.flyte.pluginportal.paper.config.language.sendError
import gg.flyte.pluginportal.paper.config.language.sendInfo
import gg.flyte.pluginportal.paper.config.language.sendSuccess
import gg.flyte.pluginportal.paper.plugin.PPPluginCache.isInstalled
import gg.flyte.pluginportal.paper.plugin.PluginManager
import kotlinx.coroutines.withContext
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Default
import revxrsal.commands.annotation.Flag
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class InstallSubCommand {

    @Subcommand("install")
    @CommandPermission("pluginportal.command.install")
    @AutoComplete("@marketplacePlugin")
    suspend fun installSubCommand(
        sender: Audience,
        @CommandManager.PPPlugin pluginName: String,
        @Flag @Optional @Default("false") @Named("isId") isId: Boolean,
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

            withContext(PluginPortal.asyncContext) {
                PluginManager.installPlugin(plugin) { success ->
                    if (success) sender.sendSuccess("Successfully installed ${plugin.getUniqueName()}")
                    else sender.sendError("Failed to install ${plugin.getUniqueName()}")
                }
            }
        }
    }

}