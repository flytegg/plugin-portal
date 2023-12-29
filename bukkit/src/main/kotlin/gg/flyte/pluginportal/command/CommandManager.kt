package gg.flyte.pluginportal.command

import gg.flyte.pluginportal.PluginPortal
import gg.flyte.pluginportal.api.type.MarketplacePlugin
import gg.flyte.pluginportal.client.PPClient
import gg.flyte.pluginportal.command.downloadable.InstallSubCommand
import gg.flyte.pluginportal.command.downloadable.UpdateSubCommand
import gg.flyte.pluginportal.command.info.HelpSubCommand
import gg.flyte.pluginportal.command.info.InfoSubCommand
import gg.flyte.pluginportal.command.info.ListSubCommand
import gg.flyte.pluginportal.manager.PPPluginCache
import gg.flyte.pluginportal.manager.PluginManager
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import revxrsal.commands.bukkit.BukkitCommandHandler

object CommandManager {

    private val mainInstance by lazy { PluginPortal.instance }

    fun init() {
        BukkitCommandHandler.create(mainInstance).apply {
            enableAdventure(BukkitAudiences.create(mainInstance))
            registerAutoComplete()
            registerCommands()
            registerHelpWriter()
            registerBrigadier()
        }
    }

    private fun BukkitCommandHandler.registerCommands() {
        register(
            InstallSubCommand(),
            UpdateSubCommand(),
            HelpSubCommand(),
            InfoSubCommand(),
            ListSubCommand(),
        )
    }

    private fun BukkitCommandHandler.registerHelpWriter() {
        setHelpWriter { command, actor ->
            if (command.path.toRealString().length > 5) {
                String.format(
                    "%s %s - %s",
                    command.path.toRealString(),
                    command.usage,
                    command.description
                )
            } else {
                null
            }
        }
    }

    private fun BukkitCommandHandler.registerAutoComplete() {
        autoCompleter
            .registerSuggestion("enabledJavaPlugins") { _, _, _ ->
                Bukkit.getPluginManager().plugins
                    .filter { it.isEnabled }
                    .map { it.name }
            }.registerSuggestion("disabledJavaPlugins") { _, _, _ ->
                Bukkit.getPluginManager().plugins
                    .filter { !it.isEnabled }
                    .map { it.name }
            }.registerSuggestion("marketplacePlugin") { args, sender, command ->
                val searchName = args[2]

                if (searchName.length == 2) {
                    PluginPortal.instance.asyncDispatch {
                        PPPluginCache.searchForPluginsByName(
                            searchName,
                        ).map { it.displayInfo.name }
                    }
                }

                if (searchName.length <= 2) {
                    return@registerSuggestion listOf("$searchName${if (searchName.isEmpty()) "" else " ~ "}Keep Typing")
                } else {
                    runBlocking {
                        PPPluginCache.searchForPluginsByName(searchName)
                            .map { it.displayInfo.name }
                            .ifEmpty { listOf("$searchName ~ No Results Found") }
                    }
                }
            }.registerSuggestion("installedPlugin") { args, sender, command ->
                PPPluginCache.getInstalledPlugins().map { it.name }.let { list ->
                    if (list.isEmpty()) {
                        return@registerSuggestion listOf("No Plugins Installed")
                    }

                    return@registerSuggestion list
                }
            }
    }

    fun getPlugins(pluginName: String, isId: Boolean) = if (isId) {
        runBlocking {
            HashSet<MarketplacePlugin>().apply { PluginManager.getPlugin(pluginName)?.let { add(it) } }
        }
    } else {
        PPPluginCache.getPluginsByName(pluginName)
            .filter { it.displayInfo.name.equals(pluginName, true) }
            .toHashSet()
    }

    @Target(AnnotationTarget.VALUE_PARAMETER)
    annotation class PPPlugin

}