package gg.flyte.pluginPortal.command.downloadable

import gg.flyte.common.api.API
import gg.flyte.common.api.plugins.schemas.InstalledPlugin
import gg.flyte.pluginPortal.command.CommandManager
import gg.flyte.pluginPortal.manager.language.sendError
import gg.flyte.pluginPortal.manager.language.sendInfo
import gg.flyte.pluginPortal.manager.language.sendSuccess
import gg.flyte.pluginPortal.manager.PPPluginCache
import gg.flyte.pluginPortal.manager.PluginManager
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

        val plugins = getPlugins(pluginName, isId).also {
            if (it.isEmpty()) return sender.sendError("No plugins found with the name '$pluginName'")
        }

        if (plugins.size > 1) {
            plugins.forEach {
                sender.sendInfo("Please specify a plugin ID with the \"-isId true\" flag.")
                sender.sendInfo(
                    " - ${it.getUniqueName()} | ${it.version ?: "Unknown Version"}",
                    false
                )
            }
        } else {
            val plugin = API.getPluginById(plugins.first().id).body() ?: return sender.sendError("Failed to find plugin with ID ${plugins.first().id}")

            if (plugin.getDownloadURL() == null)
                return sender.sendError("No download URL found for ${plugin.displayInfo.name}")

            sender.sendInfo("Updating ${plugin.getUniqueName()}")

            PluginManager.installPlugin(plugin) { success ->
                if (success) sender.sendSuccess("Successfully updated ${plugin.getUniqueName()}")
                else sender.sendError("Failed to update ${plugin.getUniqueName()}")
            }

        }

    }

    private fun getPlugins(pluginName: String, isId: Boolean) = if (isId) {
        HashSet<InstalledPlugin>()
            .apply {
                PPPluginCache.getInstalledPlugins()
                    .firstOrNull { it.id == pluginName }?.let { add(it) }
            }
    } else {
        PPPluginCache.getInstalledPlugins()
            .filter { it.name.equals(pluginName, true) }
            .toHashSet()
    }

}