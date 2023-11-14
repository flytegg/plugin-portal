package gg.flyte.pluginPortal.command.info

import gg.flyte.pluginPortal.type.manager.PPPluginCache
import gg.flyte.pluginPortal.type.manager.language.Message.toComponent
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class ListSubCommand {

    @Subcommand("list", "ls")
    @CommandPermission("pluginportal.list")
    fun listSubCommand(sender: Audience) {
        PPPluginCache.getInstalledPlugins().forEach {
            sender.sendMessage(it.name.toComponent())
        }
    }

}