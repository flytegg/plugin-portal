package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.chat.*
import gg.flyte.pluginportal.plugin.logging.Paste
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pluginportal config")
class ConfigSubCommand {

    @Subcommand("refresh")
    @CommandPermission("pluginportal.manage.config")
    fun refreshConfig(audience: Audience) {
        LocalPluginCache.reloadCache()
        audience.sendSuccess("`Refreshed config")
    }

}