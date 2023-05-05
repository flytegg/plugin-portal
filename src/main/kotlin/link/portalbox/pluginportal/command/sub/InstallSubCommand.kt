package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.Config
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.util.*
import link.portalbox.pplib.manager.MarketplacePluginManager
import link.portalbox.pplib.type.MarketplacePlugin
import link.portalbox.pplib.util.getURL
import link.portalbox.pplib.util.requestPlugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class InstallSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size <= 1) {
            sender.sendMessage("&cPlease specify a plugin to install!".colorOutput())
            return
        }

        var pluginName = ""
        if (args[1].contains(":")) {
            val split: List<String> = args[1].split(":")
            if (split.size == 2) {
                pluginName = "${split[0].uppercase()}:${split[1]}"
            }
        }

        if (!getMarketplaceCache().inverse().containsKey(pluginName)) {
            sender.sendMessage("&cYou specified an invalid plugin.".colorOutput())
            return
        }

        val id: String = getMarketplaceCache().inverse()[pluginName]?: return
        if (Data.installedPlugins.find { it.id.equals(id) } != null) {
            sender.sendMessage("&7Plugin is already installed.".colorOutput())
            return
        }

        val plugin: MarketplacePlugin = MarketplacePluginManager.getPlugin(id)
        if (plugin.isPremium) {
            sender.sendMessage("&cThis plugin is premium so you can't download it through PP. Purchase: https://www.spigotmc.org/resources/${plugin.id}".colorOutput())
            return
        }

        if (plugin.downloadURL.isEmpty()) {
            sender.sendMessage("&7We couldn't find a download link for &c$pluginName &7. This happens when they use an external link and we can't always identify the correct file to download. We have automatically sent this to our staff members".colorOutput())
            addValueToPieChart(Chart.MOST_INVALID_DOWNLOADS, plugin.id)
            requestPlugin(plugin.toRequestPlugin("External Download URL"))
            return
        }

        if (plugin.service != Config.marketplaceService) {
            sender.sendMessage("&7This plugin is not available on &c${Config.marketplaceService?.name}, change the supported service in the config.yml or buy premium (Coming Soon)!".colorOutput())
            return
        }

        sender.sendMessage("&a$pluginName &7is being installed...".colorOutput())

        Bukkit.getScheduler().runTaskAsynchronously(pluginPortal, Runnable {
            install(plugin, getURL(plugin.downloadURL)!!)
            sender.sendMessage(("&a$pluginName &7has been installed." +
                if (Config.startupOnInstall) " Plugin has automatically started but contain issues. A restart may be needed for plugin to take effect."
                else " Please restart your server for the install to take effect.").colorOutput())
        })
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null
        return if (args[1].length <= 2) {
            mutableListOf("Keep Typing...")
        } else copyPartialMatchesWithService(args[1], getMarketplaceCache().values, mutableListOf()).toMutableList()

    }
}
