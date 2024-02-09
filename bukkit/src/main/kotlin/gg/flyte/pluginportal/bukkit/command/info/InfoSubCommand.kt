package gg.flyte.pluginportal.bukkit.command.info

import gg.flyte.pluginportal.bukkit.PluginPortal
import gg.flyte.pluginportal.bukkit.command.CommandManager
import gg.flyte.pluginportal.bukkit.command.info.display.InfoDisplay
import gg.flyte.pluginportal.bukkit.manager.language.sendError
import gg.flyte.pluginportal.bukkit.manager.language.sendInfo
import io.papermc.lib.PaperLib.isPaper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience
import org.bukkit.command.CommandSender
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Commands
import org.incendo.cloud.annotations.Default
import org.incendo.cloud.annotations.Flag
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.suggestion.Suggestions
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.kotlin.coroutines.suspendingSuggestionProvider
import org.incendo.cloud.suggestion.Suggestion
import java.util.stream.Stream

object InfoSubCommand {

    @Command("pluginportal|pp|ppm info [pluginName]")
    @Permission("pluginportal.command.info")
    fun onInfoSubCommand(
        sender: Audience,

        @Argument(
            value = "pluginName",
            description = "The name of the plugin you want to install.",
            suggestions = "marketplace-plugin",
        ) pluginName: String?,

        @Flag(value =  "isId") isId: Boolean = false
//        @CommandManager.PPPlugin @Named("pluginName") @Optional pluginName: String? = null,
//        @Flag @Optional @Default("false") isId: Boolean,
    ) {

        if (pluginName.isNullOrBlank()) {

            return sender.sendInfo(
                """Usage: /pp info <plugin>
 - Plugin Portal Version: ${PluginPortal.instance.description.version}
 - Native Support: ${isPaper()}
 - Outdated: ${"TODO"}
                         """
            )
        }

        val plugins = CommandManager.getPlugins(pluginName, isId).also {
            if (it.isEmpty()) return sender.sendError("No plugins found with the name '$pluginName'")
        }

        if (plugins.size > 1) {
            plugins.forEach { plugin ->
                sender.sendInfo(" - ${plugin.getUniqueName()} | ${plugin.releaseData.latestVersion}", false)
            }
        } else {
            val plugin = plugins.first()

            sender.sendMessage(InfoDisplay.DefaultDisplay().getDisplayInfo(plugin))
        }
    }

    @Suggestions("marketplace-plugin")
    suspend fun suggestPluginNames(
        context: CommandContext<CommandSender>,
        input: String
    ): Sequence<Suggestion> = sequenceOf("ViaVersion", "ViaBackwards", "ViaRewind").map(Suggestion::simple)

}