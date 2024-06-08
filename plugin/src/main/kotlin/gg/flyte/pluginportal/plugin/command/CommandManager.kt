package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.PluginPortal.Companion.instance
import gg.flyte.pluginportal.plugin.http.SearchPlugins
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
            .registerSuggestion("plugin") { args, sender, command ->
                val searchName = args[2]

                if (searchName.length <= 1) {
                    return@registerSuggestion listOf("$searchName${if (searchName.isEmpty()) "" else " ~ "}Keep Typing")
                }

                return@registerSuggestion SearchPlugins.search(searchName)
                    .map { plugin -> plugin.name }

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