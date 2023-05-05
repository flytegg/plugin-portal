package link.portalbox.pluginportal.command

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.sub.*
import link.portalbox.pluginportal.util.color
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.util.StringUtil
import java.util.*
import java.util.stream.Collectors

class PPCommand(pluginPortal: PluginPortal) : CommandExecutor, TabCompleter {
    private val subcommands = HashMap<SubCommandType, SubCommand>().apply {
        put(SubCommandType.HELP, HelpSubCommand())
        put(SubCommandType.PREVIEW, PreviewSubCommand())
        put(SubCommandType.INSTALL, InstallSubCommand(pluginPortal))
        put(SubCommandType.LIST, ListSubCommand())
        put(SubCommandType.UPDATE, UpdateSubCommand(pluginPortal))
        put(SubCommandType.DELETE, DeleteSubCommand(pluginPortal))
        put(SubCommandType.UPDATEALL, UpdateAllSubCommand(pluginPortal))
        put(SubCommandType.REQUEST, RequestSubCommand())
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var type = if (args.isEmpty()) SubCommandType.HELP else null
        if (args.isNotEmpty()) {
            type = SubCommandType.values().find { args[0].equals(it.name, true) || args[0].equals(it.alias, true) }
            if (type == null) {
                sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &7Illegal arguments provided. Try &b/pp help&7.".color())
                return false
            }
        }

        if (!sender.hasPermission(type!!.permission)) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &7You do not have permission.".color())
            return false
        }

        subcommands[type]!!.execute(sender, args)
        return false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        if (args.size == 1) {
            return StringUtil.copyPartialMatches(
                    args[0],
                    Arrays.stream(SubCommandType.values())
                            .map { subCommandEnum -> subCommandEnum.command.lowercase(Locale.getDefault()) }
                            .collect(Collectors.toList()),
                    ArrayList())
        }

        val type = SubCommandType.values().find { args[0].equals(it.name, true) || args[0].equals(it.alias, true) }
                ?: return null
        if (!sender.hasPermission(type.permission)) {
            return null
        }

        return subcommands[type]!!.tabComplete(sender, args)
    }
}