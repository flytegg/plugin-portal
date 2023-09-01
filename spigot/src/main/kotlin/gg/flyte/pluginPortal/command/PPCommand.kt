package gg.flyte.pluginPortal.command

import gg.flyte.pluginPortal.type.language.Message.toComponent
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Default
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission
import revxrsal.commands.help.CommandHelp


@Command("pp", "pluginportal", "ppm", "pportal")
class PPCommand  {

    @DefaultFor("pp", "pluginportal", "ppm", "pportal")
    fun onPPCommand(sender: Audience, helpEntries: CommandHelp<String>, page: Int = 1) {
        for (entry in helpEntries.paginate(page, 7)) {
            sender.sendMessage(entry!!.toComponent())
        }

    }

    @Subcommand("help")
    fun onHelpCommand(sender: Audience, helpEntries: CommandHelp<String?>, @Default("1") page: Int) {
        for (entry in helpEntries.paginate(page, 7))  // 7 entries per page
            sender.sendMessage(entry!!.toComponent())
    }

    @Subcommand("install", "i")
    @CommandPermission("pluginportal.install")
    fun onInstallCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

    @Subcommand("uninstall")
    @CommandPermission("pluginportal.uninstall")
    fun onUninstallCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

    @Subcommand("preview", "p")
    @CommandPermission("pluginportal.preview")
    fun onPreviewCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

    @Subcommand("enable", "en")
    @CommandPermission("pluginportal.enable")
    fun onEnableCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

    @Subcommand("disable")
    @CommandPermission("pluginportal.disable")
    fun onDisableCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

    @Subcommand("update", "up")
    @CommandPermission("pluginportal.update")
    fun onUpdateCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

    @Subcommand("list", "ls")
    @CommandPermission("pluginportal.list")
    fun onListCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

    @Subcommand("info")
    @CommandPermission("pluginportal.info")
    fun onInfoCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

    @Subcommand("search", "s")
    @CommandPermission("pluginportal.search")
    fun onSearchCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

    @Subcommand("request")
    @CommandPermission("pluginportal.request")
    fun onRequestCommand(int: Int) {
        Bukkit.broadcast(int.toString().toComponent())
    }

}