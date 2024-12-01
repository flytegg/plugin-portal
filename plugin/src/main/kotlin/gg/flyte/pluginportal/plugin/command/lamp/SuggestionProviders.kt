package gg.flyte.pluginportal.plugin.command.lamp

import gg.flyte.pluginportal.common.SearchPlugins
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.PluginPortal.Companion.instance
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.util.async
import gg.flyte.pluginportal.plugin.util.isJarFile
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.node.ExecutionContext
import java.io.File

abstract class CustomSuggestionProvider(val suggestions: (ExecutionContext<BukkitCommandActor>) -> List<String>): SuggestionProvider<BukkitCommandActor> {
    override fun getSuggestions(context: ExecutionContext<BukkitCommandActor>) = suggestions(context)
}

class MarketplacePluginSuggestionProvider: CustomSuggestionProvider({
    val searchName = it.input().peekRemaining() //  it.input().source().split(" ").last() // TODO: Not quote aware
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
    LocalPluginCache.map(LocalPlugin::name)
})

class InstalledPluginNotPortalSuggestionProvider: CustomSuggestionProvider({
    LocalPluginCache.map(LocalPlugin::name).filter { it != instance.description.name }
})

class PluginJarFilesSuggestionProvider: CustomSuggestionProvider({
    File("plugins").listFiles()!!.filter(File::isJarFile).map(File::getName)
})