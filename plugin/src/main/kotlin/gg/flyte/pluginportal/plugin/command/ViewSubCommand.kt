package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.plugin.chat.boxed
import gg.flyte.pluginportal.plugin.chat.sendPluginListMessage
import gg.flyte.pluginportal.plugin.manager.MarketplacePluginCache
import gg.flyte.pluginportal.plugin.util.getImageComponent
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class ViewSubCommand {

    @Subcommand("view")
    @AutoComplete("@marketplacePluginSearch")
    @CommandPermission("pluginportal.view")
    fun viewCommand(
        audience: Audience,
        name: String,
        @Optional platform: MarketplacePlatform? = null,
        @Switch("byId") byId: Boolean = false,
    ) {
        MarketplacePluginCache.handlePluginSearchFeedback(
            audience,
            name,
            platform,
            byId,
            ifSingle = { audience.sendMessage(it.getImageComponent().boxed()) },
            ifMore = { sendPluginListMessage(audience, "Multiple plugins found, click one to view more information", it, "view") }
        )
    }
}
