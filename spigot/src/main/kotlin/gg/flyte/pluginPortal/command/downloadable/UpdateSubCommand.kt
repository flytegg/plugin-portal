package gg.flyte.pluginPortal.command.downloadable

import gg.flyte.pluginPortal.command.CommandManager
import gg.flyte.pluginPortal.type.extension.sendError
import gg.flyte.pluginPortal.type.extension.sendInfo
import gg.flyte.pluginPortal.type.extension.sendSuccess
import gg.flyte.pluginPortal.type.manager.PPPluginCache.isInstalled
import gg.flyte.pluginPortal.type.manager.PluginManager
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class UpdateSubCommand {

    @Subcommand("update", "u")
    @CommandPermission("pluginportal.update")
    @AutoComplete("@installedPlugin")
    fun updateSubCommand(
        sender: Audience,
        @CommandManager.PPPlugin pluginName: String,
        @Flag @Optional @Default("false") isId: Boolean,
    ) {

        val plugins = InstallSubCommand.getPlugins(pluginName, isId)
            .filter { it.isInstalled() }
            .also {
                if (it.isEmpty()) return sender.sendError("No plugins found with the name '$pluginName'")
            }

        if (plugins.size > 1) {
            plugins.forEach {
                sender.sendInfo("Please specify a plugin ID with the \"-isId true\" flag.")
                sender.sendInfo(
                    " - ${it.displayInfo.name} (${it.id}) | ${it.primaryServiceType} | ${it.versionData.latestVersion}",
                    false
                )
            }
        } else {
            val plugin = plugins.first()

            if (plugin.versions[plugin.versionData.latestVersion]?.downloadUrl == null)
                return sender.sendError("No download URL found for ${plugin.displayInfo.name}")

            sender.sendInfo("Downloading ${plugin.getUniqueName()}")

            PluginManager.installPlugin(plugin) { success ->
                if (success) sender.sendSuccess("Successfully installed ${plugin.getUniqueName()}")
                else sender.sendError("Failed to install ${plugin.getUniqueName()}")
            }

        }

    }

}