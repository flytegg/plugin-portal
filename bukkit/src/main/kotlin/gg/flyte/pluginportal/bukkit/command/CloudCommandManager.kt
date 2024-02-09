package gg.flyte.pluginportal.bukkit.command

import gg.flyte.pluginportal.bukkit.PluginPortal
import gg.flyte.pluginportal.bukkit.command.info.HelpSubCommand
import gg.flyte.pluginportal.bukkit.command.info.MenuSubCommand
import io.ktor.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minecraft.extras.AudienceProvider
import org.incendo.cloud.minecraft.extras.MinecraftHelp
import org.incendo.cloud.paper.PaperCommandManager

object CloudCommandManager {

    val audiences: BukkitAudiences by lazy { BukkitAudiences.create(PluginPortal.instance) }

    init {
        PaperCommandManager.createNative(
            PluginPortal.instance,
            ExecutionCoordinator.simpleCoordinator()
        ).apply {
            val help: MinecraftHelp<CommandSender> = MinecraftHelp.builder<CommandSender>()
                .commandManager(this)
                .audienceProvider(AudienceProvider.nativeAudience())
                .commandPrefix("/ppm")
                .colors(
                    MinecraftHelp.helpColors(
                        NamedTextColor.DARK_GRAY, NamedTextColor.AQUA,
                        NamedTextColor.GRAY, NamedTextColor.GRAY, NamedTextColor.DARK_GRAY
                    )
                )
                .build()

            AnnotationParser(this, CommandSender::class.java).apply {
                parse(MenuSubCommand)
                parse(HelpSubCommand(help))
            }

        }
    }
}