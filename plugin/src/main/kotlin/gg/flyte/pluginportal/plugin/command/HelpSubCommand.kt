package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.chat.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.annotation.CommandPermission
import revxrsal.commands.help.Help
import kotlin.math.min

private const val ENTRIES_PER_PAGE = 10

@Command("pp", "pluginportal", "ppm")
@CommandPermission("pluginportal.view")
class HelpSubCommand {

    @Subcommand("help")
    @CommandPermission("pluginportal.view")
    fun helpCommand(
        audience: Audience,

        @Named("page") @Range(min = 1.0) @Default("1") @Optional page: Int = 1,

        commands: Help.RelatedCommands<BukkitCommandActor>
    ) {
        val filteredCommands = commands.all().filter { it.usage().startsWith("pp ") }

        val totalPages = (filteredCommands.size + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE
        val safePage = page.coerceIn(1, totalPages)
        val startIndex = (safePage - 1) * ENTRIES_PER_PAGE
        val endIndex = min(startIndex + ENTRIES_PER_PAGE, filteredCommands.size)

        val paginatedEntries = filteredCommands.subList(startIndex, endIndex)

        var message = centerComponentLine(
            textPrimary("Plugin Portal - Page $safePage/$totalPages").bold()
        ).appendNewline().append(
            centerComponentLine(
                textSecondary("by ").append(text("Flyte", NamedTextColor.AQUA)
                    .showOnHover("Click here to join our Discord", NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.openUrl("https://discord.gg/flyte"))
                )
            )
        ).appendNewline().appendNewline()

        paginatedEntries.forEach { entry ->
            message = message.append(
                textSecondary(" - ").appendPrimary(entry.usage())
            ).appendNewline()
        }

        audience.sendMessage(
            message.boxed()
        )
    }
}
