package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.chat.Status
import gg.flyte.pluginportal.plugin.chat.boxed
import gg.flyte.pluginportal.plugin.chat.status
import gg.flyte.pluginportal.plugin.util.SharedComponents
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class PremiumSubCommands {

    @Subcommand("recognize")
    @CommandPermission("pluginportal.manage.recognize")
    fun recognizeCommand(audience: Audience) = premiumCommand(audience)

    @Subcommand("scan")
    @CommandPermission("pluginportal.manage.scan")
    fun scanCommand(audience: Audience) = premiumCommand(audience)

    @Subcommand("import")
    @CommandPermission("pluginportal.manage.import")
    fun importCommand(audience: Audience) = premiumCommand(audience)

    @Subcommand("export")
    @CommandPermission("pluginportal.manage.export")
    fun exportCommand(audience: Audience) = premiumCommand(audience)

    fun premiumCommand(audience: Audience) {
        audience.sendMessage(
            status(Status.FAILURE, "\n- This command is only available for Plugin Portal Premium.\n")
                .append(text("- Join our ", NamedTextColor.GRAY))
                .append(SharedComponents.DISCORD_COMPONENT)
                .append(text(" For more information", NamedTextColor.GRAY))
                .boxed()
        )
    }
}