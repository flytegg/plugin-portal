package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.plugin.manager.MarketplacePluginCache
import gg.flyte.pluginportal.plugin.chat.boxed
import gg.flyte.pluginportal.plugin.util.getImageComponent
import gg.flyte.pluginportal.plugin.chat.sendFailureMessage
import gg.flyte.pluginportal.plugin.chat.sendPluginListMessage
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.*

@Command("pp", "pluginportal", "ppm")
class ViewSubCommand {

    @Subcommand("view")
    @AutoComplete("@marketplacePluginSearch *")
    fun viewCommand(
        audience: Audience,
        @Optional prefix: String? = null,
        @Optional @Flag("platform") platformFlag: MarketplacePlatform? = null,
        @Optional @Flag("id") idFlag: String? = null,
    ) {
        if (prefix == null && idFlag == null) {
            return sendFailureMessage(audience, "No plugin name or ID provided")
        }

        val plugins = MarketplacePluginCache.getFilteredPlugins(
            prefix = prefix,
            platform = platformFlag,
            id = idFlag
        ).ifEmpty {
            return sendFailureMessage(audience, "No plugins found")
        }

        if (plugins.size == 1) {
            return audience.sendMessage(plugins.first().getImageComponent().boxed())
        }

        sendPluginListMessage(audience, "Multiple plugins found, click one to view more information", plugins, "view")
    }
}
