package link.portalbox.pluginportal.command

import link.portalbox.pluginportal.command.sub.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.util.*
import kotlin.collections.HashMap

class PPCommand : CommandExecutor, TabCompleter {

    private val subcommands = HashMap<SubCommandType, SubCommand>().apply {
        put(SubCommandType.HELP, HelpSubCommand())
        put(SubCommandType.PREVIEW, PreviewSubCommand())
        put(SubCommandType.INSTALL, InstallSubCommand())
        put(SubCommandType.LIST, ListSubCommand())
        put(SubCommandType.UPDATE, UpdateSubCommand())
        put(SubCommandType.DELETE, DeleteSubCommand())

    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var type = SubCommandType.HELP
        if (args.isNotEmpty()) {
            try {
                type = SubCommandType.valueOf(args[0].uppercase(Locale.getDefault()))
            } catch (e: IllegalArgumentException) {
                sender.sendMessage("Invalid argument provided.")
                return false;
            }
        }

        if (!sender.hasPermission(type.permission)) {
            sender.sendMessage("no permiission")
            return false;
        }

        subcommands[type]!!.execute(sender, args)
        return false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        TODO("Not yet implemented")
    }

}