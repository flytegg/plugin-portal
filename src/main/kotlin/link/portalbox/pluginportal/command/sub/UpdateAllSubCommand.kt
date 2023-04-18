package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.file.Config
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.util.color
import link.portalbox.pluginportal.util.colorOutput
import link.portalbox.pluginportal.util.delete
import link.portalbox.pluginportal.util.install
import link.portalbox.pplib.manager.MarketplacePluginManager
import link.portalbox.pplib.type.MarketplacePlugin
import link.portalbox.pplib.type.MarketplaceService
import link.portalbox.pplib.util.getURL
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class UpdateAllSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        // /pp updateall

        val needUpdating = mutableListOf<MarketplacePlugin>()
        for (plugin in Data.installedPlugins) {
            val spigetPlugin = MarketplacePluginManager.getPlugin(MarketplaceService.SPIGOTMC, plugin.id)
            if (spigetPlugin.version != plugin.version) {
                needUpdating.add(spigetPlugin)
            }
        }

        if (needUpdating.isEmpty()) {
            sender.sendMessage("&7No plugins require an update.".color())
            return
        }

        sender.sendMessage("&7Updating plugins...".color())
        for (plugin in needUpdating) {
            val id = MarketplacePluginManager.marketplaceCache.inverse()[plugin.name]
            val localPlugin = Data.installedPlugins.find { it.id == id } ?: return

            val plugin: MarketplacePlugin = MarketplacePluginManager.getPlugin(MarketplaceService.SPIGOTMC, id!!)
            if (plugin.version == localPlugin.version) return

            if (plugin.downloadURL.isEmpty()) {
                sender.sendMessage("&7We couldn't find a download link for &c${plugin.name}&7. This happens when they use an external link and we can't always identify the correct file to download. Please report this to our Discord @ discord.gg/portalbox so we manually support this.".colorOutput())
                return
            }

            sender.sendMessage("&a${plugin.name} &7is being updated...".colorOutput())

            if (!delete(pluginPortal, localPlugin)) {
                sender.sendMessage("&c${plugin.name} &7has not been updated due to an error. Tip: the plugin must be enabled before you delete it. Did you restart the server after installing?".colorOutput())
                return
            }

            Bukkit.getScheduler().runTaskAsynchronously(pluginPortal, Runnable {
                install(plugin, getURL(plugin.downloadURL)!!)
                sender.sendMessage(("&a${plugin.name} &7has been updated." +
                        if (Config.startupOnInstall) " Plugin has automatically started but contain issues. A restart may be needed for plugin to take effect."
                        else " Please restart your server for the install to take effect.").colorOutput())
            })
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        return mutableListOf()
    }
}