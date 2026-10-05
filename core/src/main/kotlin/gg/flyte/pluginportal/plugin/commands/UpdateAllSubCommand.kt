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
import gg.flyte.pluginportal.common.util.SharedComponents
import gg.flyte.pluginportal.common.util.async
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.Switch
import gg.flyte.pluginportal.common.commands.lamp.CommandPermission

@Command("pp", "pluginportal", "ppm")
class UpdateAllSubCommand {


    private fun String.plural(num: Int, suffix: String = "s") = this + (if (num == 1) "" else suffix)
    private fun String.plural(list: Collection<*>, suffix: String = "s") = plural(list.size, suffix)

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

                val eligible = LocalPluginCache.filter { !it.excludedFromUpdates }
                val excluded = LocalPluginCache.filter { it.excludedFromUpdates }
                excluded.forEach { audience.sendInfo("Skipped ${it.name}: excluded from updateAll") }
                if (eligible.isEmpty()) return@async audience.sendInfo("No plugins eligible for update.")

                val marketplacePlugins = API.getAllPluginsByPlatformIds(eligible.map { it.platformWithId })
                    ?: return@async audience.sendFailure("Failed to fetch plugin details from marketplace")
                val updates = linkedMapOf<LocalPlugin, Pair<Plugin, gg.flyte.pluginportal.common.types.Version>>()
                var skipped = excluded.size
                for (local in eligible) {
                    try {
                        val remote = marketplacePlugins[local.platform]?.get(local.platformId)
                        if (remote == null) {
                            skipped++
                            audience.sendInfo("Skipped ${local.name}: marketplace data is unavailable")
                            continue
                        }
                        val target = local.targetUpdateVersion(remote, includeCurrent = true)
                        if (target == null) {
                            skipped++
                            audience.sendInfo("Skipped ${local.name}: no compatible version found")
                        } else if (ignoreOutdated || local.targetUpdateVersion(remote) != null) {
                            updates[local] = remote to target
                        }
                    } catch (e: Exception) {
                        skipped++
                        audience.sendInfo("Skipped ${local.name}: could not check for updates")
                    }
                }
                if (updates.isEmpty()) {
                    return@async audience.sendInfo(
                        if (skipped == 0) "All plugins are up to date!"
                        else "No updates to install. $skipped plugins skipped."
                    )
                }

                // Show update list
                var messageComponent = Component.text()
                    .append(startLine())
                    .append(status(Status.INFO, "Found ${updates.size} ${"plugin".plural(updates.size)} to update:"))
                    .append(Component.newline())
                    .append(Component.newline())

                updates.forEach { (local, resolved) ->
                    val currentVersion = local.version
                    val newVersion = resolved.second.versionNumber

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

                for ((localPlugin, resolved) in updates) {
                    val (marketplacePlugin, targetVersion) = resolved
                    try {
                        val platform = localPlugin.platform
                        val targetMessage = "${localPlugin.name} from $platform with ID ${localPlugin.platformId}"
                        PortalLogger.log(audience, PortalLogger.Action.INITIATED_UPDATE, targetMessage)

                        val response = localPlugin.installUpdate(audience, true, marketplacePlugin, targetVersionOverride = targetVersion)

                        if (response.success) {
                            successCount++
                            audience.sendMessage(
                                SharedComponents.successfullyUpdatedPlugin(
                                    localPlugin.name,
                                    localPlugin.version,
                                    response.meta?.version ?: targetVersion.versionNumber,
                                    platform,
                                )
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
                        "Update complete: $successCount updated, $failCount failed, $skipped skipped"
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
