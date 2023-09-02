package gg.flyte.pluginPortal

import gg.flyte.pluginPortal.command.PPCommand
import gg.flyte.pluginPortal.command.downloadable.DeleteSubCommand
import gg.flyte.pluginPortal.command.downloadable.InstallSubCommand
import gg.flyte.pluginPortal.command.downloadable.UpdateSubCommand
import gg.flyte.pluginPortal.command.info.HelpSubCommand
import gg.flyte.pluginPortal.command.javaPlugin.DisableSubCommand
import gg.flyte.pluginPortal.command.javaPlugin.EnableSubCommand
import gg.flyte.pluginPortal.command.javaPlugin.ReloadSubCommand
import gg.flyte.pluginPortal.type.Config
import io.papermc.lib.PaperLib
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitCommandHandler


class PluginPortal : JavaPlugin() {

    private lateinit var customHelpEntries: List<String>

    override fun onEnable() {
        Config.init(this)

        val audiences = BukkitAudiences.create(this)

        val plugins = mutableSetOf(Bukkit.getPluginManager().plugins.map { it.name })

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
        logger.info("PluginPortal has been disabled!")
    }

}