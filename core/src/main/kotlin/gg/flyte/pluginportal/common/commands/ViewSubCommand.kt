package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.chat.sendPluginListMessage
import gg.flyte.pluginportal.common.commands.lamp.MarketplacePluginSuggestionProvider
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.getImageComponent
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.*
import gg.flyte.pluginportal.common.commands.lamp.CommandPermission

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
            ifSingle = { plugin ->
                if (audience.isConsole()) {
                    var details = textPrimary(plugin.name).bold()
                        .appendNewline().appendSecondary(plugin.sanitisedDescription ?: "")
                        .appendNewline().appendSecondary("Downloads: ${plugin.totalDownloads}")
                    plugin.platforms.asList().forEach { entry ->
                        details = details.appendNewline().appendPrimary("${entry.platform}: ${entry.platformId}")
                            .appendNewline().appendSecondary(entry.webpageURL)
                            .appendNewline().appendSecondary("/pp install \"${entry.platformId}\" ${entry.platform} --byId")
                    }
                    audience.sendMessage(details.boxed())
                } else audience.sendMessage(plugin.getImageComponent().boxed())
            },
            ifMore = { sendPluginListMessage(audience, "Choose a plugin to view", it, "view") }
        )
    }
}
