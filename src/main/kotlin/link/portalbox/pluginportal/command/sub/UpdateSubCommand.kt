package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.util.ChatColor.color
import org.bukkit.command.CommandSender

class UpdateSubCommand : SubCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>) {

        if (args.size >= 2) {
            val name = args[1]
            // GET ID FROM MARKETPLACE API (args[1]) REPLACE 5 BELOW
            val plugin = Data.getPlugin(5)
            if (plugin == null) {
                sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &c$name &7is not installed. Did you mean to run &b/PP install $name?".color())
                return
            }

            // CHECK IF UPDATE NEEDED WIHT API
            val outdated = true
            if (!outdated) {
                sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &7$name is already up to date.".color())
                return
            }

            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &7Updating &b$name...")
            // DOWNLOAD NEW VERSION, REMOVE OLD VERSION
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &b$name &7has been updated.".color())
            return
        }

        val needUpdating = mutableListOf<Int>()
        for (plugin in Data.installedPlugins) {
            // QUERY: IF SPIGET LATEST VERSION IS DIFF TO plugin.version
        }

        if (needUpdating.isEmpty()) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &7No plugins require an update.".color())
        } else {
            sender.sendMessage("\"&7&l[&b&lPP&7&l] &8&l> &7Listing all plugins that can be updated:".color())
            for (pluginId in needUpdating) {
                // marketplace manager, show name
            }
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>) {
        TODO("Not yet implemented")
    }

}