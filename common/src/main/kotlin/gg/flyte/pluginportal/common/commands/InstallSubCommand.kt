package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.Constants
import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.adapters.DownloadManager
import gg.flyte.pluginportal.common.adapters.DownloadRequest
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.EnabledCommand
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.commands.lamp.MarketplacePluginSuggestionProvider
import gg.flyte.pluginportal.common.commands.lamp.ReleaseChannelSuggestionProvider
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.ExactVersionSelection
import gg.flyte.pluginportal.common.types.exactCompatibleVersion
import gg.flyte.pluginportal.common.types.newestCompatibleVersion
import gg.flyte.pluginportal.common.types.newestCompatibleVersionWithFallback
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.SharedComponents
import gg.flyte.pluginportal.common.util.async
import gg.flyte.pluginportal.common.util.currentServerTypePreference
import gg.flyte.pluginportal.common.util.download
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission
import java.io.File

@Command("pp", "pluginportal", "ppm")
class InstallSubCommand {

    @EnabledCommand(Features.INSTALL)
    @Subcommand("install")
    @CommandPermission("pluginportal.manage.install")
    fun installCommand(
        audience: Audience,
        @Named("name") @SuggestWith(MarketplacePluginSuggestionProvider::class) name: String,
        @Optional @Named("platform") platform: MarketplacePlatform? = null,
        @Optional @Named("channel") @SuggestWith(ReleaseChannelSuggestionProvider::class) channel: String? = null,
        @Optional @Switch("byId") byId: Boolean = false,
        @Optional @Switch(value="exact", shorthand='e') exact: Boolean = false,
        @Optional @Flag("version") versionNumber: String? = null,
    ) {
        
        // Handle marketplace plugin installation
        MarketplacePluginCache.handlePluginSearchFeedback(
            audience,
            name,
            platform,
            byId,
            exact = exact,
            ifSingle = { plugin ->
                audience.sendMessage(startLine().appendSecondary("Starting installation of ").appendPrimary(plugin.name).appendSecondary("...").appendNewline())
                async {
//                    val request = DownloadRequest(
//                        plugin = plugin,
//                        targetDirectory = File("plugins"),
//                        versionFilter = channel,
//                        audience = audience
//                    )

                    if (platform != null && !Config.isDownloadPlatformEnabled(platform)) {
                        audience.sendFailure("Downloading from ${platform.name} is disabled in config.yml")
                        return@async
                    }

                    val targetChannel = channel?.takeIf { it.isNotBlank() }
                    val targetPlatform = platform?.let { plugin.platform(it) } ?: plugin.platforms.bestDownloadable
                    if (targetPlatform == null) {
                        audience.sendFailure("No compatible version found for ${targetChannel ?: "the default channel"}")
                        return@async
                    }

                    val serverTypes = currentServerTypePreference()
                    val exactVersionNumber = versionNumber?.takeIf { it.isNotBlank() }
                    val targetVersion = if (exactVersionNumber != null) {
                        val selection = targetPlatform.exactCompatibleVersion(exactVersionNumber, targetChannel, serverTypes)
                            .let { initial ->
                                if (initial != ExactVersionSelection.NotFound) initial
                                else API.getPluginVersions(targetPlatform.platformWithId)
                                    ?.toList()
                                    ?.exactCompatibleVersion(exactVersionNumber, targetChannel, serverTypes)
                                    ?: initial
                            }

                        when (selection) {
                            is ExactVersionSelection.Found -> selection.version
                            is ExactVersionSelection.Ambiguous -> {
                                audience.sendFailure("Multiple compatible versions named $exactVersionNumber exist: ${selection.channels.joinToString(", ")}. Rerun with /pp install \"$name\" ${targetPlatform.platform.name} <channel> --exact --version \"$exactVersionNumber\".")
                                return@async
                            }
                            ExactVersionSelection.NotFound -> {
                                audience.sendFailure("No compatible version found for $exactVersionNumber${targetChannel?.let { " on channel $it" } ?: ""}")
                                return@async
                            }
                        }
                    } else {
                        targetPlatform.newestCompatibleVersionWithFallback(targetChannel, serverTypes) {
                            API.getPluginVersions(targetPlatform.platformWithId)?.toList()
                        }
                    }

                    if (targetVersion == null) {
                        audience.sendFailure("No compatible version found for ${targetChannel ?: "the default channel"}")
                        return@async
                    }

                    val newPlugin = plugin.download(
                        update = false,
                        marketplacePlatform = targetPlatform.platform,
                        audience = audience,
                        version = targetVersion,
                        preferredChannel = if (exactVersionNumber != null) targetVersion.releaseChannel else targetChannel ?: targetVersion.releaseChannel,
                        excludedFromUpdates = exactVersionNumber != null,
                    )

//                    val result = DownloadManager.download(request)
                    
                    if (newPlugin != null) {
                        audience.sendMessage(SharedComponents.successfullyInstalledPlugin(plugin.name, newPlugin.platform))
                        if (exactVersionNumber != null) audience.sendInfo("${plugin.name} is excluded from updateAll because you installed a specific version.")
                    } else {
                        // Send this as any failure would have been of itself, boxed.
                        audience.sendMessage(endLine())
                        // This should be handled already
//                        audience.sendFailure("Installation failed when attempting to install ${plugin.name} (${plugin.id})")
                    }
                }
            },
            ifMore = {
                sendPluginListMessage(
                    audience,
                    "Multiple plugins found, click one to prompt install command",
                    it,
                    "install",
                    buildInstallSuggestionSuffix(platform, channel, exact, versionNumber)
                )
            }
        )
    }

    private fun buildInstallSuggestionSuffix(
        platform: MarketplacePlatform?,
        channel: String?,
        exact: Boolean,
        versionNumber: String?,
    ): String {
        val parts = mutableListOf<String>()
        if (!channel.isNullOrBlank()) parts += channel
        if (exact) parts += "--exact"
        versionNumber?.takeIf { it.isNotBlank() }?.let { parts += "--version \"$it\"" }
        return if (parts.isEmpty()) "" else " ${parts.joinToString(" ")}"
    }
}
