package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.chat.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission
import revxrsal.commands.command.ExecutableCommand
import revxrsal.commands.help.Help
import kotlin.math.min

@Command("pp", "pluginportal", "ppm")
@CommandPermission("pluginportal.view")
class HelpSubCommand {

    // Does not need to contain all commands, will pull commands containing these phrases to the top
    private val HELP_SORT_ORDER = listOf("update", "pp install", "uninstall", "view", "list")

    @Subcommand("help")
    @CommandPlaceholder
    @CommandPermission("pluginportal.view")
    fun helpCommand(
        audience: Audience,

        @Named("page") @Range(min = 1.0) @Default("1") @Optional page: Int = 1,

        commands: Help.RelatedCommands<BukkitCommandActor>
    ) {
        val filteredCommands = commands.all()
            .filter { it.usage().startsWith("pp ") }
            .filter { !it.usage().contains("delete") }

        val sortedCommands = mutableSetOf<ExecutableCommand<BukkitCommandActor>>()

        HELP_SORT_ORDER.forEach { order ->
            sortedCommands.add(filteredCommands.find { it.usage().contains(order) }!!)
        }

        sortedCommands.addAll(filteredCommands) // Add the rest

        var message = centerComponentLine(
            textPrimary("Plugin Portal").bold()
        ).appendNewline().append(
            centerComponentLine(
                textSecondary("by ").append(text("Flyte", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                    .showOnHover("Click here to join our Discord", NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.openUrl("https://discord.gg/flyte"))
                )
            )
        ).appendNewline().appendNewline()

        sortedCommands.forEach { entry ->
            message = message.append(
                textSecondary(" - ").appendPrimary(entry.usage())
            ).appendNewline()
        }

        audience.sendMessage(
            message.boxed()
        )
    }
}
