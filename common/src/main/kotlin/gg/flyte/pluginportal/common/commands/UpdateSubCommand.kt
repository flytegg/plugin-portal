package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.EnabledCommand
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.commands.lamp.InstalledPluginSuggestionProvider
import gg.flyte.pluginportal.common.commands.lamp.ReleaseChannelSuggestionProvider
import gg.flyte.pluginportal.common.logging.PortalLogger
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.managers.LocalPluginCache.installUpdate
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.ExactVersionSelection
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.types.Version
import gg.flyte.pluginportal.common.types.exactCompatibleVersion
import gg.flyte.pluginportal.common.util.SharedComponents
import gg.flyte.pluginportal.common.util.async
import gg.flyte.pluginportal.common.util.currentServerTypePreference
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class UpdateSubCommand {

    @EnabledCommand(Features.UPDATE)
    @Subcommand("update")
    @CommandPermission("pluginportal.maintain.update")
    fun updateCommand(
        audience: Audience,
        @Named("name") @SuggestWith(InstalledPluginSuggestionProvider::class) name: String,
        @Switch("byId") byId: Boolean = false, // @Suggest("--byId", "--ignoreOutdated", "-b", "-i", "-bi")
        @Switch("ignoreOutdated") ignoreOutdated: Boolean = false,
        @Optional @Flag("channel") @SuggestWith(ReleaseChannelSuggestionProvider::class) channel: String? = null,
        @Optional @Flag("version") versionNumber: String? = null,
    ) {
        LocalPluginCache.searchPluginsWithFeedback(
            audience,
            name,
            byId,
            ifSingle = { plugin: LocalPlugin -> handleSinglePlugin(audience, plugin, ignoreOutdated, channel, versionNumber) }.async(),
            ifMore = { plugins: List<LocalPlugin> ->
                sendLocalPluginListMessage(
                    audience,
                    "Multiple plugins found, click one to prompt update command",
                    plugins,
                    "update",
                    buildUpdateSuggestionSuffix(ignoreOutdated, channel, versionNumber)
                )
            },

        )
    }

    private fun handleSinglePlugin(audience: Audience, localPlugin: LocalPlugin, ignoreOutdated: Boolean, channel: String?, versionNumber: String?) {
        val targetChannel = channel?.takeIf { it.isNotBlank() }
        val exactVersionNumber = versionNumber?.takeIf { it.isNotBlank() }
        var marketplacePluginOverride: Plugin? = null
        var targetVersionOverride: Version? = null

        if (exactVersionNumber != null) {
            val marketplacePlugin = MarketplacePluginCache.getCachedPluginById(localPlugin.platform, localPlugin.platformId)
                ?: runCatching { MarketplacePluginCache.getOrFetchPluginById(localPlugin.platform, localPlugin.platformId) }.getOrNull()
                ?: return audience.sendFailure("Could not find plugin in marketplace (${localPlugin.name} with ID ${localPlugin.platformId} on ${localPlugin.platform})")
            val platformPlugin = marketplacePlugin.platform(localPlugin.platform)
                ?: return audience.sendFailure("${localPlugin.name} is not available on ${localPlugin.platform}")

            val selection = platformPlugin.exactCompatibleVersion(exactVersionNumber, targetChannel, currentServerTypePreference())
                .let { initial ->
                    if (initial != ExactVersionSelection.NotFound) initial
                    else API.getPluginVersions(platformPlugin.platformWithId)
                        ?.toList()
                        ?.exactCompatibleVersion(exactVersionNumber, targetChannel, currentServerTypePreference())
                        ?: initial
                }

            targetVersionOverride = when (selection) {
                is ExactVersionSelection.Found -> selection.version
                is ExactVersionSelection.Ambiguous -> {
                    audience.sendFailure("Multiple compatible versions named $exactVersionNumber exist: ${selection.channels.joinToString(", ")}. Rerun with /pp update \"${localPlugin.platformId}\" --byId --channel <name> --version \"$exactVersionNumber\".")
                    return
                }
                ExactVersionSelection.NotFound -> {
                    audience.sendFailure("No compatible version found for $exactVersionNumber${targetChannel?.let { " on channel $it" } ?: ""}")
                    return
                }
            }
            marketplacePluginOverride = marketplacePlugin

            val selectedVersion = targetVersionOverride ?: return audience.sendFailure("No compatible version found for $exactVersionNumber")
            val alreadyOnExactVersion = localPlugin.matchesVersion(selectedVersion)
            if (alreadyOnExactVersion && !ignoreOutdated) {
                localPlugin.preferredChannel = selectedVersion.releaseChannel
                localPlugin.excludedFromUpdates = true
                LocalPluginCache.save()
                audience.sendSuccess("${localPlugin.name} is already on $exactVersionNumber and is excluded from updateAll.")
                return
            }
        } else if (targetChannel != null) {
            localPlugin.preferredChannel = targetChannel
            LocalPluginCache.save()
        }

        if (exactVersionNumber == null && !ignoreOutdated && localPlugin.isUpToDate) {
            return audience.sendSuccess("Plugin is already up to date")
        }

        audience.sendMessage(startLine().appendSecondary("Starting update of ").appendPrimary(localPlugin.name).appendSecondary("...").appendNewline())

        val targetPlatform = localPlugin.platform
        val targetMessage = "${localPlugin.name} from $targetPlatform with ID ${localPlugin.platformId}"

        PortalLogger.log(audience, PortalLogger.Action.INITIATED_UPDATE, targetMessage)

        val response = localPlugin.installUpdate(
            audience,
            ignoreOutdated,
            marketplacePluginOverride,
            targetVersionOverride,
            preferredChannelOverride = targetVersionOverride?.releaseChannel,
            excludedFromUpdatesOverride = if (exactVersionNumber != null) true else localPlugin.excludedFromUpdates,
        )

        if (response.success) {
            audience.sendMessage(SharedComponents.successfullyInstalledPlugin(localPlugin.name, targetPlatform))
            if (exactVersionNumber != null) audience.sendInfo("${localPlugin.name} is excluded from updateAll because you installed a specific version.")
        } else {
            response.alertFailure(audience)
        }
    }

    private fun buildUpdateSuggestionSuffix(ignoreOutdated: Boolean, channel: String?, versionNumber: String?): String {
        val parts = mutableListOf<String>()
        if (ignoreOutdated) parts += "--ignoreOutdated"
        channel?.takeIf { it.isNotBlank() }?.let { parts += "--channel \"$it\"" }
        versionNumber?.takeIf { it.isNotBlank() }?.let { parts += "--version \"$it\"" }
        return if (parts.isEmpty()) "" else " ${parts.joinToString(" ")}"
    }
}
