package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.util.ChatColor.color
import link.portalbox.pluginportal.util.install
import link.portalbox.pplib.manager.MarketplaceManager
import link.portalbox.pplib.type.SpigetPlugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import java.net.URL

class InstallSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size <= 1) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &cPlease specify a plugin to install!".color())
            return
        }

        if (!MarketplaceManager.marketplaceCache.inverse().containsKey(args[1])) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &cYou specified an invalid plugin.".color())
            return
        }

        val id = MarketplaceManager.getId(args[1])
        if (Data.installedPlugins.find { it.id == id } != null) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &7Plugin is already installed.".color())
            return
        }

        val spigetPlugin = SpigetPlugin(id)
        if (spigetPlugin.premium) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &cThis plugin is premium so you can't download it through PP. Purchase: https://www.spigotmc.org/resources/108700".color())
            return
        }

        val downloadUrl = if (spigetPlugin.externalUrl == null) "https://api.spiget.org/v2/resources/${spigetPlugin.id}/download" else spigetPlugin.externalUrl
        if (downloadUrl == null) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &7We couldn't find a download link for &c${args[1]}&7. This happens when they use an external link and we can't always identify the correct file to download. Please report this to our Discord @ discord.gg/portalbox so we manually support this.".color())
            return
        }

        sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &a${args[1]} &7is being installed...".color())

        Bukkit.getScheduler().runTaskAsynchronously(pluginPortal, Runnable {
            install(spigetPlugin, URL(downloadUrl))
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &a${args[1]} &7has been installed. Please restart your server for the download to take effect (we are adding auto starting soon!).".color())
        })
     }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null
        return if (args[1].length <= 2) {
            mutableListOf("Keep Typing...")
        } else StringUtil.copyPartialMatches(
            args[1],
            MarketplaceManager.marketplaceCache.values,
            mutableListOf())
    }

}