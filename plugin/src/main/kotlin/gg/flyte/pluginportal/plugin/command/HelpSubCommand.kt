package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.chat.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
@CommandPermission("pluginportal.view")
class HelpSubCommand {

    val helpEntries = listOf(
        HelpEntry("/pp help", "Displays this help message"),
        HelpEntry("/pp list", "List installed plugins"),
        HelpEntry("/pp view <plugin>", "Search and view a plugin"),
        HelpEntry("/pp install <plugin>", "Install a plugin"),
        HelpEntry("/pp update <plugin>", "Update a plugin"),
        HelpEntry("/pp delete <plugin>", "Uninstall a plugin"),
        HelpEntry("/pp recognize <file>", "Recognize a plugin file")
    )

    @Subcommand("help")
    @CommandPermission("pluginportal.view")
    fun helpCommand(audience: Audience) {
        var message = centerMessage("<bold>Plugin Portal</bold> by Flyte")
            .color(NamedTextColor.AQUA)
            .appendNewline()
            .appendNewline()

        helpEntries.forEachIndexed { index, entry ->
            if (index > 0) { message = message.append(text("\n")) }
            message = message.append(entry.toComponent())
        }

        audience.sendMessage(
            message
                .boxed()
        )
    }

    @DefaultFor("~")
    fun help(audience: Audience) = helpCommand(audience)

    data class HelpEntry(val command: String, val description: String) {
        fun toComponent() = text(command)
            .colorPrimary()
            .appendDark(" - ")
            .appendSecondary(description)
    }
}