package gg.flyte.pluginportal.plugin.commands

import gg.flyte.pluginportal.common.PlatformId
import gg.flyte.pluginportal.common.chat.Status
import gg.flyte.pluginportal.common.chat.boxed
import gg.flyte.pluginportal.common.chat.status
import gg.flyte.pluginportal.common.chat.textSecondary
import gg.flyte.pluginportal.common.commands.lamp.EnabledCommand
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.logging.Paste
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.plugin.commands.lamp.RequiresAuth
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class ExportSubCommand {

    @RequiresAuth
    @EnabledCommand(Features.EXPORT)
    @Subcommand("export")
    @CommandPermission("pluginportal.manage.export")
    fun exportCommand(audience: Audience) {
        val platformIds = LocalPluginCache.map { plugin ->
            PlatformId(plugin.platformId, plugin.platform)
        }

        val exportJson = GSON.toJson(platformIds)

        // Upload to MCLogs
        Paste.upload(exportJson).let { url ->
            audience.sendMessage(
                status(Status.SUCCESS, "Exported ${platformIds.size} plugins to")
                    .append(textSecondary(" $url").clickEvent(ClickEvent.openUrl(url)))
                    .boxed()
            )
        }
    }
}