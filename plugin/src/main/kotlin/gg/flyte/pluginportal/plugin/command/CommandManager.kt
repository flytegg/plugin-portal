package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.SearchPlugins
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.PluginPortal.Companion.instance
import gg.flyte.pluginportal.plugin.command.lamp.LampExceptionHandler
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.util.async
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import revxrsal.commands.bukkit.BukkitCommandHandler
import java.io.File

object CommandManager {

    init {
        BukkitCommandHandler.create(instance).apply {
            flagPrefix = "--"

            val audiences = BukkitAudiences.create(instance)

            enableAdventure(audiences)
            registerAutoComplete()
            registerCommands()
            registerBrigadier()
            exceptionHandler = LampExceptionHandler(audiences)
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
            PremiumSubCommands(),
            DumpSubCommand(),
        )
    }

    private fun BukkitCommandHandler.registerAutoComplete() {
        autoCompleter
            .registerSuggestion("marketplacePluginSearch") { args, _, _ ->
                val searchName = args.last()

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

            }
            .registerSuggestion("installedPluginSearch") { args, _, _ ->
                LocalPluginCache.map(LocalPlugin::name)
            }
            .registerSuggestion("installedPluginSearchWithoutSelf") { args, _, _ ->
                LocalPluginCache.map(LocalPlugin::name)
                    .filter { name -> name != instance.description.name }
            }
            .registerSuggestion("pluginFileSearch") { args, _, _ ->
                File("plugins").listFiles()!!
                    .filter { file -> file.isFile }
                    .filter { file -> file.name.endsWith(".jar") }
                    .map { file -> file.name }
            }
    }
}