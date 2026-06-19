package gg.flyte.pluginportal.common.commands.lamp

import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.SearchPlugins
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.async
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.stream.StringStream

abstract class CustomSuggestionProvider(val suggestions: (ExecutionContext<BukkitCommandActor>) -> List<String>): SuggestionProvider<BukkitCommandActor> {
    override fun getSuggestions(context: ExecutionContext<BukkitCommandActor>) = suggestions(context)
}

class MarketplacePluginSuggestionProvider: CustomSuggestionProvider({
    val searchName = it.input().toArgs().last()
    if (searchName.length == 2) async { SearchPlugins.search(searchName) }
    if (searchName.length <= 2)
        listOf("Keep typing...")
    else {
        SearchPlugins.getCachedSearch(searchName)
            ?.map(Plugin::name)
            ?.filter { it.startsWith(searchName, true) }
            ?.map { "\"$it\"" }
            ?: listOf("Loading...")
    }
})

class InstalledPluginSuggestionProvider: CustomSuggestionProvider({
    LocalPluginCache.map { "\"${it.name}\"" }
})

class InstalledPluginNotPortalSuggestionProvider: CustomSuggestionProvider({
    LocalPluginCache.map(LocalPlugin::name).filter { it != PluginPortalBase.plugin.description.name }
})

class KeyActionSuggestionProvider: CustomSuggestionProvider({
    listOf("get", "set", "clear")
})

class ReleaseChannelSuggestionProvider: CustomSuggestionProvider({ context ->
    val args = context.input().toArgs()
    val command = args.getOrNull(1)?.lowercase()
    val channels = when (command) {
        "install" -> installChannelSuggestions(args)
        "update" -> updateChannelSuggestions(args)
        else -> emptyList()
    }

    channels.ifEmpty { listOf("release", "beta", "alpha") }
})

private fun installChannelSuggestions(args: List<String>): List<String> {
    val name = args.getOrNull(2)?.trim('"') ?: return emptyList()
    val platform = args.getOrNull(3)?.let(MarketplacePlatform::of)
    val plugins = SearchPlugins.getCachedSearch(name) ?: return emptyList()
    val plugin = plugins.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: plugins.firstOrNull()
    val platformPlugin = platform?.let { plugin?.platform(it) } ?: plugin?.platforms?.bestDownloadable
    return platformPlugin?.versions?.mapNotNull { it.releaseChannel }?.distinctChannels() ?: emptyList()
}

private fun updateChannelSuggestions(args: List<String>): List<String> {
    val name = args.getOrNull(2)?.trim('"') ?: return emptyList()
    val byId = args.any { it.equals("--byId", ignoreCase = true) }
    val localPlugin = if (byId) LocalPluginCache.find { it.platformId == name }
        else LocalPluginCache.firstOrNull { it.name.equals(name, ignoreCase = true) }
    if (localPlugin == null) return emptyList()
    return (MarketplacePluginCache.getCachedPluginById(localPlugin.platform, localPlugin.platformId)
        ?: runCatching { localPlugin.marketplacePlugin }.getOrNull())
        ?.platform(localPlugin.platform)
        ?.versions
        ?.mapNotNull { it.releaseChannel }
        ?.distinctChannels()
        ?: emptyList()
}

private fun List<String>.distinctChannels(): List<String> =
    distinctBy { it.lowercase() }.sorted()

private fun StringStream.toArgs(): List<String> {
    var args = mutableListOf<String>()
    var c = ""
    val words = source().split(" ")
    for (word in words) {
        if (c.isEmpty()) {
            if (!word.startsWith("\"")) args += word
            else {
                c += word.substringAfter("\"")
                if (c.contains("\"")) {
                    args.add(c.substringBefore("\""))
                    c = ""
                }
            }
        } else {
            if (word.contains("\"")) {
                args.add("$c ${word.substringBefore("\"")}")
                c = ""
            } else {
                c += " $word"
            }
        }
    }

    if (c.isNotEmpty()) args.add(c.substringBefore("\""))
    if (words.last() == "\"" && source().count { it == '"' } % 2 != 0) args += ""
    return args
}
