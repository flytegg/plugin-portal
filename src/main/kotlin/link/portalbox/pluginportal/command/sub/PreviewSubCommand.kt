package link.portalbox.pluginportal.command.sub

import gg.flyte.pplib.util.getPluginFromName
import link.portalbox.pluginportal.command.SubCommand
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.util.sendPreview
import gg.flyte.pplib.util.searchPlugins
import net.kyori.adventure.audience.Audience
import org.bukkit.command.CommandSender

class PreviewSubCommand : SubCommand() {
    override fun execute(audience: Audience, args: Array<out String>) {
        if (args.size <= 1) {
            sender.sendMessage(Message.noPluginSpecified)
            return
        }

        val plugin = getPluginFromName(args[1]) ?: run {
            sender.sendMessage(Message.pluginNotFound)
            return
        }

        sendPreview(sender, plugin)
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): MutableList<String>? {
        if (args.size != 2) return null

        return if (args[1].length <= 2) {
            mutableListOf(Message.keepTyping)
        } else {
            val completion = searchPlugins(args[1])
            if (completion.isEmpty()) {
                mutableListOf(Message.noPluginsFound)
            } else {
                completion.toMutableList()
            }
        }
    }
}
