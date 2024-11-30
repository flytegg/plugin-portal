package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.PluginPortal.Companion.instance
import gg.flyte.pluginportal.plugin.command.lamp.LampExceptionHandler
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import revxrsal.commands.Lamp
import revxrsal.commands.bukkit.BukkitLamp
import revxrsal.commands.bukkit.actor.BukkitCommandActor

object CommandManager {

    init {
        val audiences = BukkitAudiences.create(instance)

        val lamp: Lamp<BukkitCommandActor> = BukkitLamp
            .builder(instance)
            .exceptionHandler(LampExceptionHandler(audiences))
            .build()
            .apply {
                registerCommands()
            }

/*        BukkitCommandHandler.create(instance).apply {
            flagPrefix = "--"

            val audiences = BukkitAudiences.create(instance)

            enableAdventure(audiences)
            registerBrigadier()
        }*/
    }

    private fun Lamp<BukkitCommandActor>.registerCommands() {
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


}