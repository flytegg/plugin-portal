package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.Config
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.util.*
import link.portalbox.pplib.manager.MarketplacePluginManager
import link.portalbox.pplib.type.MarketplacePlugin
import link.portalbox.pplib.type.MarketplaceService
import link.portalbox.pplib.util.getURL
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

class UpdateSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size >= 2) {
            val id = getMarketplaceCache().inverse()[args[1]]
            val localPlugin = Data.installedPlugins.find { it.id == id }
            if (localPlugin == null) {
                sender.sendMessage("&c${args[1]} &7is not installed or recognised by Plugin Portal. Did you mean to run &b/pp install ${args[1]}&7?".colorOutput())
                return
            }

            val plugin: MarketplacePlugin = MarketplacePluginManager.getPlugin(id!!)
            if (plugin.version == localPlugin.version) {
                sender.sendMessage("&a${args[1]} &7is already up to date.".colorOutput())
                return
            }

            if (plugin.downloadURL.isEmpty()) {
                sender.sendMessage("&7We couldn't find a download link for &c${args[1]}&7. This happens when they use an external link and we can't always identify the correct file to download. Please report this to our Discord @ discord.gg/portalbox so we manually support this.".colorOutput())
                return
            }

            sender.sendMessage("&a${args[1]} &7is being updated...".colorOutput())

            if (!delete(pluginPortal, localPlugin)) {
                sender.sendMessage("&c${args[1]} &7has not been updated due to an error. Tip: the plugin must be enabled before you delete it. Did you restart the server after installing?".colorOutput())
                return
            }

            Bukkit.getScheduler().runTaskAsynchronously(pluginPortal, Runnable {
                install(plugin, getURL(plugin.downloadURL)!!)
                sender.sendMessage(("&a${args[1]} &7has been updated." +
                    if (Config.startupOnInstall) " Plugin has automatically started but contain issues. A restart may be needed for plugin to take effect."
                    else " Please restart your server for the install to take effect.").colorOutput())
            })
            return
        }

        val needUpdating = mutableListOf<MarketplacePlugin>()
        for (plugin in Data.installedPlugins) {
            val spigetPlugin = MarketplacePluginManager.getPlugin(plugin.id)
            if (spigetPlugin.version != plugin.version) {
                needUpdating.add(spigetPlugin)
            }
        }

        if (needUpdating.isEmpty()) {
            sender.sendMessage("&7No plugins require an update.".color())
            return
        }

        sender.sendMessage("&7Listing all plugins that can be updated:".color())
        for (spigetPlugin in needUpdating) {
            sender.sendMessage("&a+ &b${spigetPlugin.name}".color())
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null
        return StringUtil.copyPartialMatches(
                args[1],
                Data.installedPlugins.map { getMarketplaceCache()[it.id] },
                mutableListOf())
    }
}