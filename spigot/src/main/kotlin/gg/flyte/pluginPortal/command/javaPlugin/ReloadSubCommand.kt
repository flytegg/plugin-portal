package gg.flyte.pluginPortal.command.javaPlugin

import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class ReloadSubCommand {

    @Subcommand("reload")
    @CommandPermission("pluginportal.reload")
    @AutoComplete("@enabledJavaPlugins")
    fun reloadSubCommand(audience: Audience, javaPlugin: String) {

    }
}
