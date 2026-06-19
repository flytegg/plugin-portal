package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.chat.boxed
import gg.flyte.pluginportal.common.chat.sendPluginListMessage
import gg.flyte.pluginportal.common.commands.lamp.MarketplacePluginSuggestionProvider
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.getImageComponent
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class ViewSubCommand {

    @Subcommand("view")
    @CommandPermission("pluginportal.view")
    fun viewCommand(
        audience: Audience,
        @Named("name") @SuggestWith(MarketplacePluginSuggestionProvider::class) name: String,
        @Optional @Named("platform") platform: MarketplacePlatform? = null,
        @Optional @Switch("byId") byId: Boolean = false,
        @Optional @Switch(value="exact", shorthand='e') exact: Boolean = false,
    ) {
        MarketplacePluginCache.handlePluginSearchFeedback(
            audience,
            name,
            platform,
            byId,
            exact = exact,
            ifSingle = { audience.sendMessage(it.getImageComponent().boxed()) },
            ifMore = { sendPluginListMessage(audience, "Multiple plugins found, click one to view more information", it, "view") }
        )
    }
}
