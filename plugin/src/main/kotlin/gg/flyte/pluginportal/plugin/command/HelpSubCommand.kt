package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.util.appendSecondary
import gg.flyte.pluginportal.plugin.util.bold
import gg.flyte.pluginportal.plugin.util.boxed
import gg.flyte.pluginportal.plugin.util.colorPrimary
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Subcommand

@Command("pp", "pluginportal", "ppm")
class HelpSubCommand {

    val helpEntries = listOf(
        HelpEntry("help", "Displays this help message"),
        HelpEntry("view", "Search and view fora plugin"),
        HelpEntry("install", "Install a plugin"),
        HelpEntry("list", "List installed plugins"),
        HelpEntry("uninstall", "Uninstall a plugin"),
    )

    @Subcommand("help")
    fun helpCommand(audience: Audience) {
        var message = text("Plugin Portal Help")
            .colorPrimary()
            .bold()

        helpEntries.forEach { message = message.append(text("\n").append(it.toComponent())) }

        audience.sendMessage(
            message.boxed()
        )
    }

    @DefaultFor("~")
    fun help(audience: Audience) = helpCommand(audience)


    data class HelpEntry(val command: String, val description: String) {
        fun toComponent() = text(command)
            .colorPrimary()
            .bold()
            .append(text(" - ", NamedTextColor.DARK_GRAY))
            .appendSecondary(description)
    }
}