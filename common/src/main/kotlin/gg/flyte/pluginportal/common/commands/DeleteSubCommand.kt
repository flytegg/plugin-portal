package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.EnabledCommand
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.commands.lamp.InstalledPluginNotPortalSuggestionProvider
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.managers.PluginModificationManager
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.util.async
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class DeleteSubCommand {

    @EnabledCommand(Features.DELETE)
    @Subcommand("delete", "uninstall")
    @CommandPermission("pluginportal.manage.uninstall")
    fun deleteCommand(
        audience: Audience,
        @Named("name") @SuggestWith(InstalledPluginNotPortalSuggestionProvider::class) name: String,
        @Switch("byId") byId: Boolean = false,
    ) {
        LocalPluginCache.searchPluginsWithFeedback(
            audience,
            name,
            byId,
            ifSingle = { plugin: LocalPlugin ->
                val response = PluginModificationManager.uninstall(audience, plugin)
                if (response.success) {
                    audience.sendMessage(
                        status(Status.SUCCESS, "${plugin.name} has been deleted\n")
                            .appendSecondary("- Please restart your server for these changes to take effect").boxed())
                } else {
                    response.alertFailure(audience)
                }
            }.async(),
            ifMore = {
                sendLocalPluginListMessage(
                    audience,
                    "Multiple plugins found, click one to prompt delete command",
                    it,
                    "delete"
                )
            },
        )
    }
}