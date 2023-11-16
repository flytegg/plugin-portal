package gg.flyte.pluginPortal.command

import gg.flyte.common.api.API
import gg.flyte.common.api.plugins.schemas.MarketplacePlugin
import gg.flyte.pluginPortal.PluginPortal
import gg.flyte.pluginPortal.command.downloadable.InstallSubCommand
import gg.flyte.pluginPortal.command.downloadable.UpdateSubCommand
import gg.flyte.pluginPortal.command.info.HelpSubCommand
import gg.flyte.pluginPortal.command.info.InfoSubCommand
import gg.flyte.pluginPortal.command.info.ListSubCommand
import gg.flyte.pluginPortal.manager.PPPluginCache
import gg.flyte.twilight.scheduler.async
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
                    async {
                        PPPluginCache.searchForPluginsByName(
                            searchName,
                        ).map { it.displayInfo.name }
                    }
                }

                if (searchName.length <= 2) {
                    return@registerSuggestion listOf("$searchName${if (searchName.isEmpty()) "" else " ~ "}Keep Typing")
                } else {
                    PPPluginCache.searchForPluginsByName(
                        searchName,
                    ).map { it.displayInfo.name }.apply {
                        if (isEmpty()) {
                            return@registerSuggestion listOf("$searchName ~ No Results Found")
                        }
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
        HashSet<MarketplacePlugin>().apply { API.getPluginById(pluginName).body()?.let { add(it) } }
    } else {
        PPPluginCache.getPluginsByName(pluginName)
            .filter { it.displayInfo.name.equals(pluginName, true) }
            .toHashSet()
    }

    @Target(AnnotationTarget.VALUE_PARAMETER)
    annotation class PPPlugin

}