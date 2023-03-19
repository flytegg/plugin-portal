package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.util.ChatColor.color
import org.bukkit.command.CommandSender

class InstallSubCommand : SubCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size <= 1) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &cPlease specify a plugin to install!".color())
            return
        }

        sender.sendMessage("acknowledgd")

//        String spigotName = args[1];
//        int id = PluginPortal.getInstance().getMarketplaceManager().getId(spigotName);
//
//        if (!PluginPortal.getInstance().getMarketplaceManager().getAllNames().contains(spigotName)) {
//            sender.sendMessage(ChatUtil.format("&7&l[&b&lPP&7&l] &8&l> &cPlugin does not exist."));
//            return;
//        }
//
//        SpigetPlugin spigetPlugin = new SpigetPlugin(id);
//
//        if (PluginPortal.getInstance().getLocalPluginManager().getPlugins().containsKey(PluginPortal.getInstance().getMarketplaceManager().getMarketplaceCache().get(id))) {
//            sender.sendMessage(ChatUtil.format("&7&l[&b&lPP&7&l] &8&l> &7Plugin is already installed."));
//            return;
//        }
//
//        if (spigetPlugin.isPremium()) {
//            sender.sendMessage(ChatUtil.format("&7&l[&b&lPP&7&l] &8&l> &cThis plugin is a premium plugin. Please purchase it on spigotmc.org to install it!"));
//            return;
//        }
//
//        if (spigetPlugin.getFileType() == FileType.EXTERNAL) {
//            if (!flags.contains(Flags.FORCE)) {
//                PreviewUtil.sendPreview((Player) sender, spigetPlugin, true);
//                return;
//            } else if (flags.contains(Flags.GITHUB)) {
//                int argIndex = 0;
//                for (String string : args) {
//                    if (string.equalsIgnoreCase("-g") || string.equalsIgnoreCase("--github")) {
//                        JsonParser parser = new JsonParser();
//                        JsonObject object = parser.parse(HttpUtil.convertGithubToApi(spigetPlugin.getExternalUrl().replace("latest", "assets?name=" + args[argIndex+1]))).getAsJsonObject();
//                        PluginPortal.getInstance().getDownloadManager().download(spigetPlugin, object.get("browser_download_url").getAsString());
//
//                    }
//
//                    argIndex++;
//                }
//            }
//        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>) {
        TODO("Not yet implemented")
    }

}