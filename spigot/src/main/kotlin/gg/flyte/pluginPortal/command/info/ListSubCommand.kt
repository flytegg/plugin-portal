package gg.flyte.pluginPortal.command.info

import gg.flyte.pluginPortal.type.extension.sendInfo
import gg.flyte.pluginPortal.type.manager.PPPluginCache
import gg.flyte.pluginPortal.type.manager.language.Message.toComponent
import gg.flyte.twilight.extension.solidLine
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class ListSubCommand {

    @Subcommand("list", "ls")
    @CommandPermission("pluginportal.list")
    fun listSubCommand(sender: Audience) {
        val strike = Component.text("").solidLine()
        with(sender) {
            sendMessage(strike)
            if (PPPluginCache.getInstalledPlugins().isEmpty()) { sendInfo("No plugins installed.") }

            PPPluginCache.getInstalledPlugins().forEach {
                sendMessage(" - ${it.getUniqueName()} | ${it.version ?: "Unknown Version"}".toComponent())
            }
            sendMessage(strike)
        }
    }

}