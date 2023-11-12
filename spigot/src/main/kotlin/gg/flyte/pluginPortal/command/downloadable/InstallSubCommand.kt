package gg.flyte.pluginPortal.command.downloadable

import gg.flyte.common.api.PPPluginCache
import gg.flyte.common.type.api.plugin.schemas.MarketplacePlugin
import gg.flyte.common.util.installPlugin
import gg.flyte.pluginPortal.type.annotation.PPPlugin
import gg.flyte.pluginPortal.type.extension.sendError
import gg.flyte.pluginPortal.type.extension.sendInfo
import gg.flyte.pluginPortal.type.extension.sendSuccess
import gg.flyte.pluginPortal.type.manager.SpigotInstalledPluginLoader
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
        @PPPlugin pluginName: String,
        @Flag @Optional @Default("false") isId: Boolean,
    ) {
        val plugins = getPlugins(pluginName, isId).also {
            if (it.isEmpty()) return sender.sendError("No plugins found with the name '$pluginName'")
        }

        if (plugins.size > 1) {
            plugins.forEach {
                sender.sendInfo(" - ${it.displayInfo.name} (${it.id}) | ${it.primaryServiceType} | ${it.versionData.latestVersion}", false)
            }
        } else {
            val plugin = plugins.first()

            if (plugin.versions[plugin.versionData.latestVersion]?.downloadUrl == null)
                return sender.sendError("No download URL found for ${plugin.displayInfo.name}")

            sender.sendInfo("Downloading ${plugin.displayInfo.name} (${plugin.id})")

            installPlugin(
                plugin,
                plugin.versions[plugin.versionData.latestVersion]!!.downloadUrl!!,
                SpigotInstalledPluginLoader.pluginFolder,
                false
            )

            sender.sendSuccess("Successfully installed ${plugin.displayInfo.name} (${plugin.id})")

        }


    }

    private fun getPlugins(pluginName: String, isId: Boolean) = if (isId) {
        HashSet<MarketplacePlugin>().apply { PPPluginCache.getPluginById(pluginName)?.let { add(it) } }
    } else {
        PPPluginCache.getPluginsByName(pluginName)
            .filter { it.displayInfo.name.equals(pluginName, true) }
            .toHashSet()
    }

}