package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.chat.sendFailure
import net.kyori.adventure.audience.Audience
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
        audience.sendFailure("This command is only available for Plugin Portal Premium.")
    }
}