package gg.flyte.pluginportal.plugin.commands

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.Constants
import gg.flyte.pluginportal.common.PlatformId
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.EnabledCommand
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.logging.Paste
import gg.flyte.pluginportal.common.logging.PortalLogger
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.SharedComponents
import gg.flyte.pluginportal.common.util.async
import gg.flyte.pluginportal.plugin.commands.lamp.RequiresAuth
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission


@Command("pp", "pluginportal", "ppm")
class ImportSubCommand {

    @RequiresAuth
    @EnabledCommand(Features.IMPORT)
    @Subcommand("import")
    @CommandPermission("pluginportal.manage.import")
    fun importCommand(
        audience: Audience,
        @Named("url") url: String
    ) {
        async {
            try {
                if (!url.contains("mclo.gs")) {
                    return@async audience.sendFailure("Invalid URL. Please provide a valid MCLogs URL")
                }

                // Initial message
                audience.sendInfo("Fetching plugin list from MCLogs...")

                // Get raw content from MCLogs
                val id = url.substringAfterLast("/")
                val content = Paste.getRawContent(id)
                val platformIds = GSON.fromJson<Array<PlatformId>>(content, Array<PlatformId>::class.java)

                if (platformIds.isEmpty()) {
                    return@async audience.sendFailure("No plugins found in the import file")
                }

                // Get plugin details from API
                val pluginMap: Map<MarketplacePlatform, Map<String, Plugin?>> = API.getAllPluginsByPlatformIds(platformIds.toList()) ?: return@async audience.sendFailure("Failed to fetch plugin details")
                val plugins = pluginMap.values.flatMap { it.entries }

                // Build initial message
                var messageComponent: TextComponent = text("")
                    .append(status(Status.INFO, "Found ${plugins.size} plugins to import:"))
                    .append(newline())
                    .append(newline())

                // List all plugins to be installed
                plugins.forEach { (id, plugin) ->
                    val platform = plugin?.bestPlatform
                    messageComponent = messageComponent
                        .append(textSecondary(" • "))
                        .append(textPrimary(plugin?.name ?: id))
                        .append(text(" from ", NamedTextColor.DARK_GRAY))
                        .append(text(platform?.name ?: "NOT FOUND", NamedTextColor.AQUA))
                        .append(newline())
                }

                audience.sendMessage(messageComponent.boxed())

                // Install each plugin
                audience.sendInfo("Starting installation of ${plugins.size} plugins...")

                var successCount = 0
                var skipCount = 0
                var failCount = 0

                for (plugin in plugins.mapNotNull { it.value }) {
                    if (LocalPluginCache.hasPlugin(plugin)) {
                        skipCount++
                        audience.sendMessage(status(Status.WARNING, "Skipping ${plugin.name} (already installed)").append(newline()))
                        continue
                    }

                    try {
                        val platform = plugin.bestPlatform

                        audience.sendMessage(
                            startLine()
                                .appendSecondary("Installing ")
                                .appendPrimary(plugin.name)
                                .append(text(" from ", NamedTextColor.DARK_GRAY))
                                .append(text(platform?.name ?: "NOT FOUND", NamedTextColor.AQUA))
                                .appendSecondary("...")
                                .append(endLine())
                        )

                        if (platform == null) return@async audience.sendFailure("Could not find an available platform")

                        val response = MarketplacePluginCache.installPlugin(
                            audience,
                            plugin,
                            platform,
                            Constants.INSTALL_DIRECTORY
                        )

                        if (response.success) {
                            audience.sendMessage(SharedComponents.successfullyInstalledPlugin(plugin.name, platform, false))
                            successCount++
                        } else {
                            response.alertFailure(audience)
                            failCount++
                        }

                        // Add delay between installations
                        Thread.sleep(250)
                    } catch (e: Exception) {
                        failCount++
                        audience.sendMessage(
                            status(Status.FAILURE, "Failed to install ${plugin.name}: ${e.message}").boxed()
                        )
                    }
                }

                // Final summary
                audience.sendMessage(
                    status(
                        if (failCount == 0) Status.SUCCESS else Status.WARNING,
                        "Import complete: $successCount installed, $skipCount skipped, $failCount failed"
                    ).boxed()
                )

            } catch (e: Exception) {
                val reason = e.message ?: e::class.simpleName
                audience.sendMessage(
                    status(Status.FAILURE, "Failed to import plugins: $reason").boxed()
                )
                PortalLogger.warn("Failed to import plugins: $reason")
            }
        }
    }
}
