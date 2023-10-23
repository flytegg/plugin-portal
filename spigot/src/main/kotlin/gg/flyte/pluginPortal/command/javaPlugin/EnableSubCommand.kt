package gg.flyte.pluginPortal.command.javaPlugin

import net.kyori.adventure.audience.Audience
import org.bukkit.World
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm", "pportal")
class EnableSubCommand {

    @Subcommand("enable")
    @CommandPermission("pluginportal.enable")
    @AutoComplete("@disabledJavaPlugins")
    fun enableSubCommand(audience: Audience, plugin: String) {

    }
}