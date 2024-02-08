package gg.flyte.pluginportal.bukkit.command

import gg.flyte.pluginportal.bukkit.PluginPortal
import org.bukkit.command.CommandSender
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.kotlin.extension.commandBuilder
import org.incendo.cloud.paper.PaperCommandManager

object CloudCommandManager {

    init {
        println("Registering commands")

        PaperCommandManager(
            PluginPortal.instance,
            ExecutionCoordinator.simpleCoordinator(),
            SenderMapper.identity()
        ).apply {
            commandBuilder("pluginportal", aliases = arrayOf("pp", "ppm", "pportal")) {
                literal("menu")
            }

            buildAndRegister("pluginportal", aliases = arrayOf("pp", "ppm", "pportal")) {
                literal("install")
            }

            buildAndRegister("pluginportal", aliases = arrayOf("pp", "ppm", "pportal")) {
                literal("update")
            }

            buildAndRegister("pluginportal", aliases = arrayOf("pp", "ppm", "pportal")) {
                literal("help")
            }

            buildAndRegister("pluginportal", aliases = arrayOf("pp", "ppm", "pportal")) {
                literal("info")
            }

            AnnotationParser(this, CommandSender::class.java).apply {
                parseContainers()
            }
        }
    }

    fun getSubCommands(vararg commands: String): HashSet<String> {
        val baseCommands = hashSetOf("pp", "pluginportal", "ppm", "pportal")

        return commands
            .map { command ->
                baseCommands
                    .map { baseCommand -> "$baseCommand $command" }
            }
            .flatten()
            .toHashSet()
    }

}