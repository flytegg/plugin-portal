package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.EnabledCommand
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.commands.lamp.InstalledPluginNotPortalSuggestionProvider
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.util.async
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.SuggestWith
import revxrsal.commands.annotation.Switch
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class BlacklistSubCommand {

    @EnabledCommand(Features.UPDATE)
    @Subcommand("blacklist")
    @CommandPermission("pluginportal.maintain.update")
    fun blacklistCommand(
        audience: Audience,
        @Optional @Named("name") @SuggestWith(InstalledPluginNotPortalSuggestionProvider::class) name: String? = null,
        @Switch("byId") byId: Boolean = false,
    ) {
        if (name.isNullOrBlank()) {
            async {
                val blacklisted = LocalPluginCache
                    .filter { it.excludedFromUpdates && it.name != PluginPortalBase.plugin.description.name }
                    .sortedBy { it.name }

                if (blacklisted.isEmpty()) return@async audience.sendInfo("No plugins are blacklisted from updateAll.")

                var message = Component.text("Plugins blacklisted from updateAll", NamedTextColor.GRAY)
                blacklisted.forEach { plugin ->
                    message = message.append(Component.newline())
                        .append(textDark(" - "))
                        .appendPrimary(plugin.name)
                        .append(textDark(" (${plugin.platform.name})"))
                }
                audience.sendMessage(message.boxed())
            }
            return
        }

        LocalPluginCache.searchPluginsWithFeedback(
            audience,
            name,
            byId,
            ifSingle = { plugin: LocalPlugin ->
                plugin.excludedFromUpdates = !plugin.excludedFromUpdates
                LocalPluginCache.save()
                val state = if (plugin.excludedFromUpdates) "blacklisted from" else "removed from the blacklist for"
                audience.sendSuccess("${plugin.name} is now $state updateAll.")
            }.async(),
            ifMore = { plugins: List<LocalPlugin> ->
                sendLocalPluginListMessage(
                    audience,
                    "Multiple plugins found, click one to toggle update blacklist",
                    plugins,
                    "blacklist"
                )
            },
        )
    }
}
