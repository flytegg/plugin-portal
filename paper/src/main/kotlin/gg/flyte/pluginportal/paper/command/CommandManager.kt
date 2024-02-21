package gg.flyte.pluginportal.paper.command

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import gg.flyte.pluginportal.api.type.MarketplacePlugin
import gg.flyte.pluginportal.paper.PluginPortal
import gg.flyte.pluginportal.paper.command.downloadable.InstallSubCommand
import gg.flyte.pluginportal.paper.command.downloadable.UpdateSubCommand
import gg.flyte.pluginportal.paper.command.info.HelpSubCommand
import gg.flyte.pluginportal.paper.command.info.InfoSubCommand
import gg.flyte.pluginportal.paper.command.info.ListSubCommand
import gg.flyte.pluginportal.paper.command.info.MenuSubCommand
import gg.flyte.pluginportal.paper.plugin.PPPluginCache
import gg.flyte.pluginportal.paper.plugin.PluginManager
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import revxrsal.commands.bukkit.BukkitCommandHandler
import revxrsal.commands.ktx.supportSuspendFunctions

object CommandManager {

    private val mainInstance by lazy { PluginPortal.instance }

    init {
        BukkitCommandHandler.create(mainInstance).apply {
            supportSuspendFunctions()
            enableAdventure(BukkitAudiences.create(mainInstance))

            registerAutoComplete()
            registerCommands()
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
            MenuSubCommand(),
        )
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
                    plugin.launch(PluginPortal.instance.asyncDispatcher, CoroutineStart.UNDISPATCHED) {
                        PPPluginCache.searchForPluginsByName(
                            searchName,
                        ).map { it.displayInfo.name }
                    }
                }

                if (searchName.length <= 2) {
                    return@registerSuggestion listOf("$searchName${if (searchName.isEmpty()) "" else " ~ "}Keep Typing").also {
                        println("Keep typing")
                    }
                } else {
                    println("Run blocking")

                    runBlocking {
                        PPPluginCache.searchForPluginsByName(searchName)
                            .map { it.displayInfo.name }
                            .ifEmpty { listOf("$searchName ~ No Results Found") }.also { println(it) }
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