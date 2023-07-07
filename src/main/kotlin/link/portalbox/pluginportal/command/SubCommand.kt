package link.portalbox.pluginportal.command

import link.portalbox.pluginportal.PluginPortal
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

abstract class SubCommand {
    abstract fun execute(audience: Audience, commandSender: CommandSender, args: Array<out String>)
    abstract fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>?

    fun executeAsync(pluginPortal: PluginPortal, audience: Audience, commandSender: CommandSender, args: Array<out String>) {
        Bukkit.getScheduler().runTaskAsynchronously(pluginPortal, Runnable {
            execute(audience, commandSender, args)
        })
    }
}