package gg.flyte.pluginPortal.command.downloadable

import gg.flyte.common.api.PPPluginCache
import gg.flyte.pluginPortal.type.annotation.PPPlugin
import gg.flyte.pluginPortal.type.extension.sendError
import gg.flyte.pluginPortal.type.extension.sendInfo
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class InstallSubCommand {

    @Subcommand("install", "i")
    @CommandPermission("pluginportal.install")
    @AutoComplete("@marketplacePlugin")
    fun installSubCommand(sender: Audience, @PPPlugin pluginName: String) {
        val plugins = PPPluginCache.getPluginsByName(pluginName)

        if (plugins.isEmpty()) return sender.sendError("No plugins found with the name '$pluginName'")

        if (plugins.size > 1) {
            plugins.forEach {
                sender.sendInfo(" - ${it.displayInfo.name} (${it.id}) | ${it.primaryServiceType} | ${it.versionData.latestVersion}")
            }
        } else {
            sender.sendInfo("install preview format")
        }


    }

}