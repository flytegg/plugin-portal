package gg.flyte.pluginportal.paper.command.downloadable

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import gg.flyte.pluginportal.api.type.CompactPlugin
import gg.flyte.pluginportal.paper.PluginPortal
import gg.flyte.pluginportal.paper.command.CommandManager
import gg.flyte.pluginportal.paper.config.language.sendError
import gg.flyte.pluginportal.paper.config.language.sendInfo
import gg.flyte.pluginportal.paper.config.language.sendSuccess
import gg.flyte.pluginportal.paper.plugin.PPPluginCache
import gg.flyte.pluginportal.paper.plugin.PluginManager
import kotlinx.coroutines.withContext
import net.kyori.adventure.audience.Audience
import org.bukkit.plugin.Plugin
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class UpdateSubCommand {

    @Subcommand("update")
    @CommandPermission("pluginportal.command.update")
    @AutoComplete("@installedPlugin")
    suspend fun updateSubCommand(
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
            withContext(PluginPortal.asyncContext) {
                val plugin = PluginManager.getPlugin(plugins.first().id)
                    ?: return@withContext sender.sendError("Failed to find plugin with ID ${plugins.first().id}")

                if (plugin.getLatestVersion()?.downloadUrl == null)
                    return@withContext sender.sendError("No download URL found for ${plugin.displayInfo.name}")

                sender.sendInfo("Updating ${plugin.getUniqueName()}")

                PluginManager.installPlugin(plugin) { success ->
                    if (success) sender.sendSuccess("Successfully updated ${plugin.getUniqueName()}")
                    else sender.sendError("Failed to update ${plugin.getUniqueName()}")
                }
            }
        }

    }

    private fun getPlugins(pluginName: String, isId: Boolean) = if (isId) {
        HashSet<CompactPlugin>()
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