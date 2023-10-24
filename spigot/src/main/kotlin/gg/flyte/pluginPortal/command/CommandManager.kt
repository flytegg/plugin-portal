package gg.flyte.pluginPortal.command

import gg.flyte.common.api.PPPluginCache
import gg.flyte.pluginPortal.PluginPortal
import gg.flyte.pluginPortal.command.downloadable.DeleteSubCommand
import gg.flyte.pluginPortal.command.downloadable.InstallSubCommand
import gg.flyte.pluginPortal.command.downloadable.UpdateSubCommand
import gg.flyte.pluginPortal.command.info.HelpSubCommand
import gg.flyte.pluginPortal.command.info.ListSubCommand
import gg.flyte.pluginPortal.command.javaPlugin.DisableSubCommand
import gg.flyte.pluginPortal.command.javaPlugin.EnableSubCommand
import gg.flyte.pluginPortal.command.javaPlugin.ReloadSubCommand
import gg.flyte.twilight.scheduler.async
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import revxrsal.commands.bukkit.BukkitCommandHandler

object CommandManager {

    private val mainInstance by lazy { PluginPortal.instance }

    // TODO: Split into multiple methods

    fun registerCommands() {
        BukkitCommandHandler.create(mainInstance).apply {
            enableAdventure(BukkitAudiences.create(mainInstance))

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
                                PlatformType.PAPER,
                            ).map { it.displayInfo.name }
                        }
                    }

                    if (searchName.length <= 2) {
                        return@registerSuggestion listOf("$searchName${if (searchName.isEmpty()) "" else " ~ "}Keep Typing")
                    } else {
                        PPPluginCache.searchForPluginsByName(
                            searchName,
                            PlatformType.PAPER,
                        ).map { it.displayInfo.name }.apply {
                            if (isEmpty()) {
                                return@registerSuggestion listOf("$searchName ~ No Results Found")
                            }
                        }
                    }
                }



            register(
                InstallSubCommand(),
                DeleteSubCommand(),
                UpdateSubCommand(),
                HelpSubCommand(),
                ListSubCommand(),
                DisableSubCommand(),
                EnableSubCommand(),
                ReloadSubCommand(),
                PPCommand(),
            )

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

            registerBrigadier()
        }
    }

}