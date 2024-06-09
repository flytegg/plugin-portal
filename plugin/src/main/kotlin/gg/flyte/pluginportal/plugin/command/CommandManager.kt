package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.PluginPortal.Companion.instance
import gg.flyte.pluginportal.plugin.http.SearchPlugins
import gg.flyte.pluginportal.plugin.util.async
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import revxrsal.commands.bukkit.BukkitCommandHandler

object CommandManager {

    init {
        BukkitCommandHandler.create(instance).apply {
            enableAdventure(BukkitAudiences.create(instance))
            registerAutoComplete()
            registerCommands()
            registerBrigadier()
        }
    }

    private fun BukkitCommandHandler.registerCommands() {
        register(
            InstallSubCommand(),
            HelpSubCommand(),
            ViewSubCommand()
        )
    }

    private fun BukkitCommandHandler.registerAutoComplete() {
        autoCompleter
            .registerSuggestion("marketplacePluginSearch") { args, sender, command ->
                val searchName = args[0]

                if (searchName.length <= 2) {
                    if (searchName.length == 2) async { SearchPlugins.search(searchName) }

                    return@registerSuggestion listOf("$searchName${if (searchName.isEmpty()) "" else " ~ "}Keep Typing")
                }


                val plugins = SearchPlugins.getCachedSearch(searchName)
                if (plugins != null) {
                    return@registerSuggestion plugins.map(Plugin::name)
                }

                return@registerSuggestion listOf("$searchName ~ Loading...")

            }
//            .registerSuggestion("installedPlugin") { args, sender, command ->
//                PPPluginCache.getInstalledPlugins().map { it.name }.let { list ->
//                    if (list.isEmpty()) {
//                        return@registerSuggestion listOf("No Plugins Installed")
//                    }
//
//                    return@registerSuggestion list
//                }
//            }
    }
}