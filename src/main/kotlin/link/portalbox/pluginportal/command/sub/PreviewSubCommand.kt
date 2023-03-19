package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.util.ChatColor.color
import org.bukkit.command.CommandSender

class PreviewSubCommand : SubCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size <= 1) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &cPlease specify a plugin to preview!".color())
            return
        }

        val spigotName = args[1]
        sender.sendMessage("acknowledged")
//        if (!marketplaceManager.getAllNames().contains(spigotName)) {
//                sender.sendMessage(ChatUtil.format("&7&l[&b&lPP&7&l] &8&l> &cPlugin does not exist."));
//                return;
//            }
//
//            int id = marketplaceManager.getId(spigotName);
//            SpigetPlugin spigetPlugin = new SpigetPlugin(id);
//
//            if (!marketplaceManager.getAllNames().contains(spigotName)) {
//                sender.sendMessage(ChatUtil.format("&7&l[&b&lPP&7&l] &8&l> &cPlugin does not exist."));
//                return;
//            }
//
//            PreviewUtil.sendPreview((Player) sender, spigetPlugin, true);
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>) {
        TODO("Not yet implemented")
    }

}