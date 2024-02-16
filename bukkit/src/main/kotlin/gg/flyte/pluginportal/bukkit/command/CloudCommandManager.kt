package gg.flyte.pluginportal.bukkit.command

import gg.flyte.pluginportal.api.type.MarketplacePlugin
import gg.flyte.pluginportal.bukkit.PluginPortal
import gg.flyte.pluginportal.bukkit.command.info.HelpSubCommand
import gg.flyte.pluginportal.bukkit.command.info.InfoSubCommand
import gg.flyte.pluginportal.bukkit.command.info.ListSubCommand
import gg.flyte.pluginportal.bukkit.command.info.MenuSubCommand
import gg.flyte.pluginportal.bukkit.manager.PPPluginCache
import gg.flyte.pluginportal.bukkit.manager.PluginManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.kotlin.coroutines.annotations.installCoroutineSupport
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
            if (hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
                registerBrigadier();
            } else if (hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
                registerAsynchronousCompletions();
            }


            val help: MinecraftHelp<CommandSender> = MinecraftHelp.builder<CommandSender>()
                .commandManager(this)
                .audienceProvider(AudienceProvider.nativeAudience())
                .commandPrefix("/pluginportal")
                .colors(
                    MinecraftHelp.helpColors(
                        NamedTextColor.DARK_GRAY, NamedTextColor.AQUA,
                        NamedTextColor.GRAY, NamedTextColor.GRAY, NamedTextColor.DARK_GRAY
                    )
                )
                .build()

            AnnotationParser(this, CommandSender::class.java).apply {
                installCoroutineSupport()

//                parse(MenuSubCommand)

                hashSetOf(
////                    MenuSubCommand,
////                    HelpSubCommand(help),
                    ListSubCommand,
//                    InfoSubCommand,
                ).forEach { command -> parse(command) }
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
}