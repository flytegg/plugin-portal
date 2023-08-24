package link.portalbox.pluginportal.command.sub

import gg.flyte.pplib.util.getPluginFromName
import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.Config
import link.portalbox.pluginportal.type.Data
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.type.language.Message.fillInVariables
import link.portalbox.pluginportal.util.*
import gg.flyte.pplib.util.searchPlugins
import link.portalbox.pluginportal.type.language.Message.deserialize
import net.kyori.adventure.audience.Audience
import org.bukkit.command.CommandSender

class InstallSubCommand(private val pluginPortal: PluginPortal) : SubCommand() {
    override fun execute(audience: Audience, commandSender: CommandSender, args: Array<out String>) {
        if (args.size <= 1) {
            audience.sendMessage(Message.noPluginSpecified)
            return
        }

        val plugin = getPluginFromName(args[1]) ?: run {
            audience.sendMessage(Message.pluginNotFound)
            return
        }

        if (Data.installedPlugins.find { it.marketplacePlugin.id == plugin.id } != null) {
            audience.sendMessage(Message.pluginAlreadyInstalled)
            return
        }

        if (plugin.isPremium) {
            audience.sendMessage(Message.pluginIsPremium)
            return
        }

        if (!plugin.isValidDownload()) {
            audience.sendMessage(Message.downloadNotFound)
            return
        }

        if (!plugin.extraInfo.isNullOrEmpty()) {
            audience.sendMessage("<gray>Extra Info: ${plugin.extraInfo}</gray>".deserialize())
        }

        if (plugin.service != Config.marketplaceService) {
            audience.sendMessage(
                Message.serviceNotSupported.fillInVariables(
                    arrayOf(
                        Config.marketplaceService ?: "UNKNOWN"
                    )
                )
            )
            // return
        }

        audience.sendMessage(Message.pluginIsBeingInstalled)

        install(plugin, pluginPortal).run {
            audience.sendMessage(Message.pluginHasBeenInstalled.fillInVariables(arrayOf(plugin.name)))
            audience.sendMessage(if (Config.startupOnInstall) Message.pluginAttemptedEnabling else Message.restartServerToEnablePlugin)
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null

        return if (args[1].length <= 2) {
            mutableListOf(Message.keepTyping)
        } else {
            pluginPortal.tabManager.searchTerms(args[1])
            val completion = copyPartialMatchesWithService(args[1], pluginPortal.tabManager.getTableComplete(args[1]), mutableListOf()).toMutableList()

            if (completion.isEmpty()) {
                mutableListOf(Message.noPluginsFound)
            } else {
                completion.toMutableList()
            }
        }
    }
}
