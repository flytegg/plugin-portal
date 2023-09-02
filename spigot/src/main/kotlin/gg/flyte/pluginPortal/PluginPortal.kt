package gg.flyte.pluginPortal

import gg.flyte.common.api.PPPluginCache
import gg.flyte.common.api.interfaces.InstalledPluginLoader
import gg.flyte.common.type.api.service.PlatformType
import gg.flyte.pluginPortal.command.PPCommand
import gg.flyte.pluginPortal.command.downloadable.DeleteSubCommand
import gg.flyte.pluginPortal.command.downloadable.InstallSubCommand
import gg.flyte.pluginPortal.command.downloadable.UpdateSubCommand
import gg.flyte.pluginPortal.command.info.HelpSubCommand
import gg.flyte.pluginPortal.command.javaPlugin.DisableSubCommand
import gg.flyte.pluginPortal.command.javaPlugin.EnableSubCommand
import gg.flyte.pluginPortal.command.javaPlugin.ReloadSubCommand
import gg.flyte.pluginPortal.type.manager.Config
import gg.flyte.pluginPortal.type.manager.SpigotInstalledPluginLoader
import gg.flyte.twilight.scheduler.async
import gg.flyte.twilight.twilight
import io.papermc.lib.PaperLib
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitCommandHandler

class PluginPortal : JavaPlugin() {

    override fun onEnable() {
        Config.init(this)
        twilight(this) {}
        PPPluginCache.loadInstalledPlugins(
            dataFolder.apply { mkdir() }.parentFile,
            SpigotInstalledPluginLoader(this).apply {
                loadInstalledPlugins()
            }
        )

        val audiences = BukkitAudiences.create(this)

        BukkitCommandHandler.create(this).apply {
            enableAdventure(audiences)

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

        Metrics(this, 18005)
        PaperLib.suggestPaper(this)
    }

    override fun onDisable() {
        PPPluginCache.saveInstalledPlugins()
        logger.info("PluginPortal has been disabled!")
    }

}