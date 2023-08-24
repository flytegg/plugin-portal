package link.portalbox.pluginportal.command

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.sub.*
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.type.language.Message.audiences
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.util.StringUtil

class PPCommand(private val pluginPortal: PluginPortal) : CommandExecutor, TabCompleter {
    private val subcommands = hashMapOf(
        SubCommandType.HELP to HelpSubCommand(),
        SubCommandType.PREVIEW to PreviewSubCommand(pluginPortal),
        SubCommandType.INSTALL to InstallSubCommand(pluginPortal),
        SubCommandType.LIST to ListSubCommand(),
        SubCommandType.UPDATE to UpdateSubCommand(pluginPortal),
        SubCommandType.DELETE to DeleteSubCommand(pluginPortal),
        SubCommandType.UPDATEALL to UpdateAllSubCommand(pluginPortal),
        SubCommandType.REQUEST to RequestSubCommand(pluginPortal)
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var type = if (args.isEmpty()) SubCommandType.HELP else null
        if (args.isNotEmpty()) {
            type = SubCommandType.byName(args[0])
            if (type == null) {
                sender.sendMessage(Message.illegalArguments)
                return false
            }
        }

        if (!sender.hasPermission(type!!.permission)) {
            sender.sendMessage(Message.noPermission)
            return false
        }



        if (type.isAsync) {
            subcommands[type]!!.executeAsync(pluginPortal, audiences.sender(sender), sender, args)
        } else {
            subcommands[type]!!.execute(audiences.sender(sender), sender, args)
        }

        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        if (args.size == 1) {
            return StringUtil.copyPartialMatches(
                args[0],
                SubCommandType.commandNames,
                ArrayList()
            )
        }

        val type = SubCommandType.byName(args[0]) ?: return null
        if (!sender.hasPermission(type.permission)) {
            return null
        }

        return subcommands[type]!!.tabComplete(sender, args)
    }
}