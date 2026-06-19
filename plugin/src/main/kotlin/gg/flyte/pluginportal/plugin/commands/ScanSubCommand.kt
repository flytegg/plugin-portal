package gg.flyte.pluginportal.plugin.commands

import gg.flyte.pluginportal.common.Constants
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.util.SharedComponents
import gg.flyte.pluginportal.plugin.commands.lamp.PluginJarFilesSuggestionProvider
import gg.flyte.pluginportal.plugin.commands.lamp.RequiresAuth
import gg.flyte.pluginportal.plugin.commands.lamp.SafeFileName
import io.papermc.hangar.scanner.HangarJarScanner
import io.papermc.hangar.scanner.model.Severity
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.SuggestWith
import revxrsal.commands.bukkit.annotation.CommandPermission
import java.io.File

@Command("pp", "pluginportal", "ppm")
class ScanSubCommand {

    @RequiresAuth
    @Subcommand("scan")
    @CommandPermission("pluginportal.manage.scan")
    fun scanCommand(audience: Audience, @Named("file")  @SuggestWith(PluginJarFilesSuggestionProvider ::class) @SafeFileName pluginFileName: String) {
        val pluginFile = runCatching { File(Constants.INSTALL_DIRECTORY, pluginFileName) }.getOrNull()

        if (pluginFile == null) {
            audience.sendMessage(text("Plugin file not found: $pluginFileName"))
            return
        }

        val alerts = HangarJarScanner().scanJar(pluginFile.inputStream(), pluginFile.name).results
            .filter { result -> result.severity().ordinal <= Severity.MEDIUM.ordinal }

        if (alerts.isEmpty())
            return audience.sendMessage(status(Status.SUCCESS, "No issues found in $pluginFileName").boxed())

        var alertMessage = textSecondary("Found ${alerts.size} issues in $pluginFileName. [HOVER]")
            .hoverEvent(
                HoverEvent.showText(
                    text("This does not mean this plugin is malicious, nearly all plugins will have some issues. Do not panic and read the issues carefully. If you are unsure, ask for help in our ")
                        .append(SharedComponents.DISCORD_COMPONENT)
                )
            )
            .appendNewline()

        alerts.forEach { alert ->
            alertMessage = alertMessage.appendNewline()
                .append(alert.severity().toDisplaySeverity().prefix())
                .append(textSecondary(" ${alert.message()} "))
                .append(
                    textDark("[MORE]").hoverEvent(HoverEvent.showText(text(alert.format())))
                )
        }

        return audience.sendMessage(alertMessage.boxed())

    }

    enum class DisplaySeverity(val color: TextColor) {
        HIGHEST(NamedTextColor.DARK_RED),
        HIGH(NamedTextColor.RED),
        MEDIUM(NamedTextColor.GOLD),
        LOW(NamedTextColor.YELLOW),
        LOWEST(NamedTextColor.GRAY),
        UNKNOWN(NamedTextColor.DARK_GRAY);

        fun prefix(): Component {
            return Component.text("[${name}]", color)
        }
    }

    fun Severity.toDisplaySeverity(): DisplaySeverity {
        return when (this) {
            Severity.HIGHEST -> DisplaySeverity.HIGHEST
            Severity.HIGH -> DisplaySeverity.HIGH
            Severity.MEDIUM -> DisplaySeverity.MEDIUM
            Severity.LOW -> DisplaySeverity.LOW
            Severity.LOWEST -> DisplaySeverity.LOWEST
            Severity.UNKNOWN -> DisplaySeverity.UNKNOWN
        }
    }
}