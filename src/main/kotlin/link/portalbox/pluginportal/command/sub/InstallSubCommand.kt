package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.file.Config
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.util.Chart
import link.portalbox.pluginportal.util.addValueToPieChart
import link.portalbox.pluginportal.util.colorOutput
import link.portalbox.pluginportal.util.install
import link.portalbox.pplib.manager.MarketplacePluginManager
import link.portalbox.pplib.type.MarketplacePlugin
import link.portalbox.pplib.type.MarketplaceService
import link.portalbox.pplib.util.getURL
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

class InstallSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size <= 1) {
            sender.sendMessage("&cPlease specify a plugin to install!".colorOutput())
            return
        }

        if (!MarketplacePluginManager.marketplaceCache.inverse().containsKey(args[1])) {
            sender.sendMessage("&cYou specified an invalid plugin.".colorOutput())
            return
        }

        val id = MarketplacePluginManager.marketplaceCache.inverse()[args[1]]
        if (Data.installedPlugins.find { it.id == id } != null) {
            sender.sendMessage("&7Plugin is already installed.".colorOutput())
            return
        }

        val plugin: MarketplacePlugin = MarketplacePluginManager.getPlugin(MarketplaceService.SPIGOTMC, id!!)
        if (plugin.isPremium) {
            sender.sendMessage("&cThis plugin is premium so you can't download it through PP. Purchase: https://www.spigotmc.org/resources/108700".colorOutput())
            return
        }

        if (plugin.downloadURL.isNullOrEmpty()) {
            sender.sendMessage("&7We couldn't find a download link for &c${args[1]}&7. This happens when they use an external link and we can't always identify the correct file to download. Please report this to our Discord @ discord.gg/portalbox so we manually support this.".colorOutput())
            addValueToPieChart(Chart.MOST_INVALID_DOWNLOADS, plugin.id)
            return
        }

        sender.sendMessage("&a${args[1]} &7is being installed...".colorOutput())

        Bukkit.getScheduler().runTaskAsynchronously(pluginPortal, Runnable {
            install(plugin, getURL(plugin.downloadURL)!!)
            sender.sendMessage(("&a${args[1]} &7has been installed." +
                if (Config.startupOnInstall) "Plugin has automatically started but contain issues. A restart may be needed for plugin to take effect."
                else "Please restart your server for the install to take effect.").colorOutput())
        })
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null
        return if (args[1].length <= 2) {
            mutableListOf("Keep Typing...")
        } else StringUtil.copyPartialMatches(
                args[1], MarketplacePluginManager.marketplaceCache.values, mutableListOf()
        )
    }
}
