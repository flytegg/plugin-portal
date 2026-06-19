package gg.flyte.pluginportal.plugin.commands

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.EnabledCommand
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.logging.PortalLogger
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.managers.LocalPluginCache.installUpdate
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.ActionResponseComponent
import gg.flyte.pluginportal.common.util.ActionResponseString
import gg.flyte.pluginportal.common.util.async
import gg.flyte.pluginportal.plugin.commands.lamp.RequiresAuth
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.Switch
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class UpdateAllSubCommand {


    private fun String.plural(num: Int, suffix: String = "s") = this + (if (num == 1) "" else suffix)
    private fun String.plural(list: Collection<*>, suffix: String = "s") = plural(list.size, suffix)

    @RequiresAuth
    @EnabledCommand(Features.UPDATE)
    @Subcommand("updateAll")
    @CommandPermission("pluginportal.maintain.update")
    fun updateAllCommand(
        audience: Audience,
        @Switch("ignoreOutdated") ignoreOutdated: Boolean = false,
    ) {
        async {
            try {
                val numLocalPlugins = LocalPluginCache.size
                if (numLocalPlugins == 0) return@async audience.sendInfo("No plugins installed to update")

                audience.sendInfo("Checking updates for $numLocalPlugins ${"plugin".plural(numLocalPlugins)}...")

                // Fetch latest plugin info from API TODO: Just use cache
                val marketplacePlugins: Map<MarketplacePlatform, Map<String, Plugin?>> = API.getAllPluginsByPlatformIds(LocalPluginCache.getAllPluginsAsPlatformIds())
                    ?.takeIf { it.isNotEmpty() }
                    ?: return@async audience.sendFailure("Failed to fetch plugin details from marketplace")

                val updates: Map<LocalPlugin, Plugin> = LocalPluginCache
                    .associateWith { lcl ->
                        marketplacePlugins[lcl.platform]?.get(lcl.platformId)
                    }
                    // Not up to date
                    .filter { (lcl, mkp) ->
                        if (mkp == null) return@filter false
                        val target = lcl.targetUpdateVersion(mkp) ?: return@filter false
                        ignoreOutdated || !lcl.matchesVersion(target)
                    }
                    // Excluded from auto-updates
                    .filter { (lcl, _) -> !lcl.excludedFromUpdates}
                    .ifEmpty { return@async audience.sendSuccess("All plugins are up to date!") }
                    .mapNotNull { (localPlugin, marketplacePlugin) ->
                        marketplacePlugin?.let { localPlugin to it }
                    }
                    .toMap()


                // Show update list
                var messageComponent = Component.text()
                    .append(startLine())
                    .append(status(Status.INFO, "Found ${updates.size} ${"plugin".plural(updates.size)} to update:"))
                    .append(Component.newline())
                    .append(Component.newline())

                updates.forEach { (local, marketplace) ->
                    val currentVersion = local.version
                    val newVersion = local.targetUpdateVersion(marketplace)?.versionNumber ?: "unknown"

                    messageComponent = messageComponent
                        .append(textSecondary(" • "))
                        .append(textPrimary(local.name))
                        .append(Component.text(" (", NamedTextColor.DARK_GRAY))
                        .append(Component.text(currentVersion, NamedTextColor.RED))
                        .append(Component.text(" → ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(newVersion, NamedTextColor.GREEN))
                        .append(Component.text(")", NamedTextColor.DARK_GRAY))
                        .append(Component.newline())
                }

                messageComponent = messageComponent.append(endLine())
                audience.sendMessage(messageComponent)

                // Start updating
                audience.sendInfo("Starting update of ${updates.size} ${"plugin".plural(updates.size)}...")

                var successCount = 0
                var failCount = 0

                for ((localPlugin, marketplacePlugin) in updates) {
                    try {
                        val platform = localPlugin.platform
                        val targetMessage = "${localPlugin.name} from $platform with ID ${localPlugin.platformId}"
                        PortalLogger.log(audience, PortalLogger.Action.INITIATED_UPDATE, targetMessage)

                        val response = localPlugin.installUpdate(audience, true, marketplacePlugin)

                        if (response.success) {
                            successCount++
                            audience.sendMessage(
                                Component.text("[SUCCESS]: ", NamedTextColor.GREEN)
                                    .appendSecondary("Updated ")
                                    .appendPrimary(localPlugin.name)
                                    .appendSecondary(" from ")
                                    .appendPrimary(platform.name)
                                    .appendSecondary("...")
                                    .boxed()
                            )
                        } else {
                            failCount++
                            val error = if (response is ActionResponseComponent) {
                                response.error
                            } else {
                                val message = (response as? ActionResponseString)?.error ?: "Unknown error"
                                Component.text("[FAILURE]: ", NamedTextColor.RED)
                                    .append(text(message))
                                    .append(endLine())
                            }
                            // God dammit I want my colour codes back
                            var comp = textSecondary("Failure updating ").appendPrimary(localPlugin.name)
                                .appendSecondary(" from ")
                                .appendPrimary(platform.name).append(Component.newline()).append(Component.newline())

                            if (error != null) comp = comp.append(error)

                            audience.sendMessage(comp)
                        }

                        // TODO: Better delay system... Why we do this
                        Thread.sleep(250)

                    } catch (e: Exception) {
                        failCount++
                        audience.sendFailure("Failed to update ${localPlugin.name}: ${e.message}")
                        PortalLogger.log(
                            audience,
                            PortalLogger.Action.FAILED_UPDATE,
                            "${localPlugin.name} from ${localPlugin.platform}"
                        )
                    }
                }

                LocalPluginCache.save()

                // Final summary
                audience.sendMessage(
                    status(
                        if (failCount == 0) Status.SUCCESS else Status.WARNING,
                        "Update complete: $successCount updated, $failCount failed"
                    ).boxed()
                )

            } catch (e: Exception) {
                val reason = e.message ?: e::class.simpleName
                audience.sendFailure("Failed to process updates: $reason")
                PortalLogger.warn("Failed to process bulk update: $reason")
            }
        }
    }
}
