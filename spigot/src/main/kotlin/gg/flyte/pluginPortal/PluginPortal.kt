package gg.flyte.pluginPortal

import gg.flyte.pluginPortal.command.PPCommand
import gg.flyte.pluginPortal.command.downloadable.DeleteSubCommand
import gg.flyte.pluginPortal.command.downloadable.InstallSubCommand
import gg.flyte.pluginPortal.command.downloadable.UpdateSubCommand
import gg.flyte.pluginPortal.command.info.HelpSubCommand
import gg.flyte.pluginPortal.command.toggle.DisableSubCommand
import gg.flyte.pluginPortal.command.toggle.EnableSubCommand
import gg.flyte.pluginPortal.type.Config
import io.papermc.lib.PaperLib
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitCommandHandler
import revxrsal.commands.orphan.OrphanCommand
import revxrsal.commands.orphan.Orphans

class PluginPortal : JavaPlugin() {

    private lateinit var customHelpEntries: List<String>

    override fun onEnable() {
        Config.init(this)

        val audiences = BukkitAudiences.create(this)

        BukkitCommandHandler.create(this).apply {
            enableAdventure(audiences)

            register(
                InstallSubCommand(),
                DeleteSubCommand(),
                UpdateSubCommand(),
                HelpSubCommand(),
                DisableSubCommand(),
                EnableSubCommand(),
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

        }.registerBrigadier()

        Metrics(this, 18005)
        PaperLib.suggestPaper(this)
    }

    override fun onDisable() {
        logger.info("PluginPortal has been disabled!")
    }

    fun BukkitCommandHandler.fastRegister(vararg commands: OrphanCommand) {
        commands.forEach { register(Orphans.path().handler(it)) }
    }

}