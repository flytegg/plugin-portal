package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.util.ChatColor.colorOutput
import link.portalbox.pluginportal.util.sendPreview
import link.portalbox.pplib.manager.MarketplaceManager
import link.portalbox.pplib.type.SpigetPlugin
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

class PreviewSubCommand : SubCommand() {

  override fun execute(sender: CommandSender, args: Array<out String>) {
    if (args.size <= 1) {
      sender.sendMessage("&cPlease specify a plugin to preview!".colorOutput())
      return
    }

    if (!MarketplaceManager.marketplaceCache.inverse().contains(args[1])) {
      sender.sendMessage("&cPlugin does not exist.".colorOutput())
      return
    }

    val spigetPlugin = SpigetPlugin(MarketplaceManager.getId(args[1]))

    sendPreview(sender, spigetPlugin, true);
  }

  override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
    if (args.size != 2) return null
    return if (args[1].length <= 2) {
      mutableListOf("Keep Typing...")
    } else StringUtil.copyPartialMatches(
      args[1], MarketplaceManager.marketplaceCache.values, mutableListOf()
    )
  }

}
