package link.portalbox.pluginportal.command.sub

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.util.ChatColor.color
import link.portalbox.pluginportal.util.delete
import link.portalbox.pluginportal.util.install
import link.portalbox.pplib.manager.MarketplaceManager
import link.portalbox.pplib.type.SpigetPlugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import java.net.URL

class UpdateSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size >= 2) {
            val id = MarketplaceManager.getId(args[1])
            val localPlugin = Data.installedPlugins.find { it.id == id }
            if (localPlugin == null) {
                sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &c${args[1]} &7is not installed or recognised by Plugin Portal. Did you mean to run &b/pp install ${args[1]}&7?".color())
                return
            }

            val spigetPlugin = SpigetPlugin(id)
            if (spigetPlugin.onlineVersion == localPlugin.version) {
                sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &a${args[1]} &7is already up to date.".color())
                return
            }

            val downloadUrl = if (spigetPlugin.externalUrl == null) "https://api.spiget.org/v2/resources/${spigetPlugin.id}/download" else spigetPlugin.externalUrl
            if (downloadUrl == null) {
                sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &7We couldn't find a download link for &c${args[1]}&7. This happens when they use an external link and we can't always identify the correct file to download. Please report this to our Discord @ discord.gg/portalbox so we manually support this.".color())
                return
            }

            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &a${args[1]} &7is being updated...".color())

            if (!delete(pluginPortal, localPlugin)) {
                sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &c${args[1]} &7has not been updated due to an error. Tip: the plugin must be enabled before you delete it. Did you restart the server after installing?".color())
                return
            }

            Bukkit.getScheduler().runTaskAsynchronously(pluginPortal, Runnable {
                install(spigetPlugin, URL(downloadUrl))

                sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &a${args[1]} &7has been updated. Please restart your server for the download to take effect (we are adding auto starting soon!).".color())
            })
            return
        }

        val needUpdating = mutableListOf<SpigetPlugin>()
        for (plugin in Data.installedPlugins) {
            val spigetPlugin = SpigetPlugin(plugin.id)
            if (spigetPlugin.onlineVersion != plugin.version) {
                needUpdating.add(spigetPlugin)
            }
        }

        if (needUpdating.isEmpty()) {
            sender.sendMessage("&7&l[&b&lPP&7&l] &8&l> &7No plugins require an update.".color())
            return;
        }

        sender.sendMessage("\"&7&l[&b&lPP&7&l] &8&l> &7Listing all plugins that can be updated:".color())
        for (spigetPlugin in needUpdating) {
            sender.sendMessage("&a+ &b${spigetPlugin.name}".color())
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null
        return StringUtil.copyPartialMatches(
            args[1],
            Data.installedPlugins.map { MarketplaceManager.getName(it.id) },
            mutableListOf())
    }

}