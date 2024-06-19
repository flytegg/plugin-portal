package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.PluginPortal.Companion.instance
import gg.flyte.pluginportal.plugin.http.SearchPlugins
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.util.async
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import revxrsal.commands.bukkit.BukkitCommandHandler

object CommandManager {

    init {
        BukkitCommandHandler.create(instance).apply {
            flagPrefix = "--"

            enableAdventure(BukkitAudiences.create(instance))
            registerAutoComplete()
            registerCommands()
            registerBrigadier()
        }
    }

    private fun BukkitCommandHandler.registerCommands() {
        register(
            InstallSubCommand(),
            UpdateSubCommand(),
            DeleteSubCommand(),
            HelpSubCommand(),
            ViewSubCommand(),
            ListSubCommand(),
        )
    }

    private fun BukkitCommandHandler.registerAutoComplete() {
        autoCompleter
            .registerSuggestion("marketplacePluginSearch") { args, _, _ ->
                val searchName = args[0]

                if (searchName.length == 2) async { SearchPlugins.search(searchName) }

                if (searchName.length <= 2)
                    listOf("$searchName${if (searchName.isEmpty()) "" else " ~ "}Keep Typing")
                else
                    SearchPlugins.getCachedSearch(searchName)?.map(Plugin::name) ?: listOf("$searchName ~ Loading")
            }

            .registerSuggestion("installedPluginSearch") { args, _, _ ->
                LocalPluginCache.map(LocalPlugin::name)
            }
    }
}