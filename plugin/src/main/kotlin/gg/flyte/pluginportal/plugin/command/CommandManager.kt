package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.plugin.PluginPortal.Companion.instance
import gg.flyte.pluginportal.plugin.command.lamp.AudienceResolver
import gg.flyte.pluginportal.plugin.command.lamp.LampExceptionHandler
import gg.flyte.pluginportal.plugin.command.lamp.MarketplacePlatformType
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
            .senderResolver(AudienceResolver(audiences))
            .parameterTypes {
                it.addParameterType(MarketplacePlatform::class.java, MarketplacePlatformType())
            }
            .build()
            .apply {
                registerCommands()
            }
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
            ConfigSubCommand()
        )
    }


}