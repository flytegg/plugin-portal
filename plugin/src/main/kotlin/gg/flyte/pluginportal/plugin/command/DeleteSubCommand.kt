package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.plugin.chat.*
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache.findAssociatedFiles
import gg.flyte.pluginportal.plugin.util.async
import gg.flyte.pluginportal.plugin.util.isPluginPortal
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class DeleteSubCommand {

    @Subcommand("delete", "uninstall")
    @AutoComplete("@installedPluginSearchWithoutSelf *")
    @CommandPermission("pluginportal.manage.uninstall")
    fun deleteCommand(
        audience: Audience,
        @Named("name") name: String,
        @Switch("byId") byId: Boolean = false,
    ) {
        LocalPluginCache.searchPluginsWithFeedback(
            audience,
            name,
            byId,
            ifSingle = { plugin: LocalPlugin -> handleSinglePlugin(audience, plugin) }.async(),
            ifMore = {
                sendLocalPluginListMessage(audience, "Multiple plugins found, click one to prompt delete command", it, "delete")
            },
        )
    }

    private fun handleSinglePlugin(audience: Audience, localPlugin: LocalPlugin) {
        val targetPlatform = localPlugin.platform
        val targetMessage = "${localPlugin.name} from $targetPlatform with ID ${localPlugin.platformId}"

        if (localPlugin.isPluginPortal) {
            return audience.sendMessage(status(Status.FAILURE, "You cannot delete Plugin Portal").boxed())
        }

        val files = localPlugin.findAssociatedFiles()

        if (files.isEmpty()) {
            LocalPluginCache.deletePlugin(localPlugin, files)
            return audience.sendMessage(status(Status.FAILURE, "Could not find plugin jar to delete").boxed())
        }

        LocalPluginCache.deletePlugin(localPlugin, files)

        PortalLogger.log(audience, PortalLogger.Action.DELETE, targetMessage)

        audience.sendMessage(status(Status.SUCCESS, "${localPlugin.name} has been deleted\n")
            .appendSecondary("- Please restart your server for these changes to take effect").boxed())
    }
}
