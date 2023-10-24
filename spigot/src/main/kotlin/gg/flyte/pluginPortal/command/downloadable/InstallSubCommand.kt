package gg.flyte.pluginPortal.command.downloadable

import gg.flyte.common.api.PPPluginCache
import gg.flyte.common.type.api.plugin.MarketplacePlugin
import gg.flyte.pluginPortal.type.annotation.PPPlugin
import gg.flyte.pluginPortal.type.extension.sendError
import gg.flyte.pluginPortal.type.extension.sendInfo
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Flag
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
        @Flag isId: Boolean = false
    ) {

        val plugins: ArrayList<MarketplacePlugin> = if (isId) {
            val plugin = PPPluginCache.getPluginById(pluginName)
            if (plugin == null) {
                sender.sendError("No plugins found with the id '$pluginName'")
                return
            }

            arrayListOf(plugin)
        } else {
            PPPluginCache.getPluginsByName(pluginName)
        }

        if (plugins.isEmpty()) return sender.sendError("No plugins found with the name '$pluginName'")

        if (plugins.size > 1) {
            plugins.forEach {
                sender.sendInfo(" - ${it.displayInfo.name} (${it.id}) | ${it.primaryServiceType} | ${it.versionData.latestVersion}")
            }
        } else {
            sender.sendInfo("Downloading ${plugins[0].displayInfo.name} (${plugins[0].id})")
        }


    }

}