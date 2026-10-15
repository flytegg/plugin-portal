package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.chat.sendFailure
import gg.flyte.pluginportal.common.chat.sendPluginSearchResultsMessage
import gg.flyte.pluginportal.common.commands.lamp.MarketplacePluginSuggestionProvider
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.async
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.Flag
import revxrsal.commands.annotation.Switch
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.SuggestWith
import gg.flyte.pluginportal.common.commands.lamp.CommandPermission

@Command("pp", "pluginportal", "ppm")
class SearchSubCommand {

    @Subcommand("search")
    @CommandPermission("pluginportal.view")
    fun searchCommand(
        audience: Audience,
        @Named("query") @SuggestWith(MarketplacePluginSuggestionProvider::class) query: String,
        @Optional @Named("platform") platform: MarketplacePlatform? = null,
        @Optional @Flag("page") page: Int? = null,
        @Switch("full") full: Boolean = false,
    ) = async {
        if (page != null && page < 1) return@async audience.sendFailure("Page must be at least 1")
        if (full && page != null) return@async audience.sendFailure("Use either --full or --page, not both")
        val plugins = MarketplacePluginCache.getFilteredPlugins(query, platform)
            .let { matches -> with(MarketplacePluginCache) { matches.sortedByRelevance(query) } }

        if (plugins.isEmpty()) {
            audience.sendFailure("No plugins found")
            return@async
        }

        val quotedQuery = query.replace("\\", "\\\\").replace("\"", "\\\"")
        val command = "/pp search \"$quotedQuery\"${platform?.let { " $it" } ?: ""}"
        sendPluginSearchResultsMessage(audience, query, plugins, page, full, command)
    }
}
