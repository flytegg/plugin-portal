package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.util.ChatColor.color
import org.bukkit.command.CommandSender

class DeleteSubCommand : SubCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size <= 1) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &cPlease specify a plugin to delete!".color())
            return
        }

        // GET ID FROM MARKETPLACE. REPLACE VALUE BELOW
        val plugin = Data.getPlugin(5)




    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>) {
        TODO("Not yet implemented")
    }

}