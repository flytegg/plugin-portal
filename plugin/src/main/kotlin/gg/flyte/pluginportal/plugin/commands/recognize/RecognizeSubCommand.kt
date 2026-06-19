package gg.flyte.pluginportal.plugin.commands.recognize

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.Constants
import gg.flyte.pluginportal.common.Hash
import gg.flyte.pluginportal.common.PlatformId
import gg.flyte.pluginportal.common.chat.sendFailure
import gg.flyte.pluginportal.common.chat.sendInfo
import gg.flyte.pluginportal.common.chat.sendSuccess
import gg.flyte.pluginportal.common.commands.lamp.EnabledCommand
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.HashType
import gg.flyte.pluginportal.common.util.async
import gg.flyte.pluginportal.common.util.isPluginPortal
import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.commands.lamp.PluginJarFilesUnrecognisedSP
import gg.flyte.pluginportal.plugin.commands.lamp.RequiresAuth
import gg.flyte.pluginportal.plugin.commands.lamp.SafeFileName
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.SuggestWith
import revxrsal.commands.bukkit.annotation.CommandPermission
import java.io.File

@Command("pp", "pluginportal", "ppm")
class RecognizeSubCommand {

    private val logger get() = PluginPortal.instance.logger

    @RequiresAuth
    @EnabledCommand(Features.RECOGNISE)
    @Subcommand("recognize")
    @CommandPermission("pluginportal.manage.recognize")
    fun recognizeCommand(
        audience: Audience,
        @Named("file") @SuggestWith(PluginJarFilesUnrecognisedSP::class) @SafeFileName pluginFileName: String
    ) {
        async {
            logger.info("[ RECOGNITION ] - Attempting to recognize $pluginFileName")
            var pluginFile: File = File(Constants.INSTALL_DIRECTORY, pluginFileName)

            if (!pluginFile.exists() || pluginFile.isDirectory) {
                // Search for a partial match
                val matching = Constants.INSTALL_DIRECTORY.listFiles().filter { it.extension == "jar" && it.name.contains(pluginFileName, true) }
                if (matching.size == 1) {
                    pluginFile = matching[0]
                    audience.sendInfo("Found partial match of '${pluginFile.name}' from '$pluginFileName'")
                } else {
                    return@async audience.sendFailure("Plugin file not found: $pluginFileName")
                }
            }

            if (pluginFile.isPluginPortal) {
                return@async audience.sendFailure("This plugin looks familiar!") // Easter egg??
            }

            val sha256 = HashType.SHA256.hash(pluginFile)
            val sha512 = HashType.SHA512.hash(pluginFile)

            // Check if already installed
            if (LocalPluginCache.hasPluginByHash(sha256)) {
                return@async audience.sendInfo("Plugin already installed")
            }

            // Check for polymart.yml file in the jar
            val polymartData = Recognize.getPolymartData(pluginFile)

            // If polymart.yml exists, handle it locally
            if (polymartData != null) {
                val (productId, title, version) = polymartData
                logger.info("Found associated polymart data, fetching additional info...")
                val plugin = API.getPluginByPlatformId(PlatformId(productId, MarketplacePlatform.POLYMART))?.platform(MarketplacePlatform.POLYMART)

                if (plugin == null) {
                    logger.info("Lookup failed for polymart plugin $title ($productId). Did not receive a valid response.")
                    return@async audience.sendFailure("No plugin found for $pluginFileName (polymart)")
                }

                logger.info("Adding Polymart plugin to cache - ID: $productId, Title: $title, Version: $version")

                LocalPluginCache.add(polymartData.getNewLocalPlugin(plugin.entryId, sha256, sha512))
                LocalPluginCache.save()

                return@async audience.sendSuccess("Found Polymart plugin: $title v$version")
            }

            val recognized = API.recognizePlugins(listOf(Hash(sha256, sha512)))?.get(sha256)
            val recognizedPlugin = recognized?.plugin

            if (recognized == null || recognizedPlugin == null) {
                logger.info("Unable to find a match for '$pluginFileName'")
                return@async audience.sendFailure("No plugin found for $pluginFileName")
            }

            val platformPlugin = recognizedPlugin.platforms.best ?: return@async audience.sendFailure("Could not find an available platform")
            val version = recognized.version
                ?: platformPlugin.newestBukkitPaperVersion(null)?.versionNumber
                ?: return@async audience.sendFailure("No compatible Bukkit/Paper version found for ${recognizedPlugin.name}")

            logger.info("Adding $platformPlugin plugin to cache ${platformPlugin.platformId} ${recognizedPlugin.name}")

            LocalPluginCache.add(
                LocalPlugin(
                    platformPlugin.entryId,
                    platformPlugin.platformId,
                    recognizedPlugin.name,
                    version,
                    platformPlugin.platform,
                    sha256,
                    sha512,
                    System.currentTimeMillis(),
                )
            )

            LocalPluginCache.save()
            // instantly rename file
            pluginFile.renameTo(File(pluginFile.parentFile, recognizedPlugin.getFullDownloadedName(platformPlugin.platform)))

            async {
                // Load into the marketplace cache.
                MarketplacePluginCache.getOrFetchPluginById(platformPlugin.platform, platformPlugin.platformId)
            }

            audience.sendSuccess("Found plugin: ${recognizedPlugin.name} from ${platformPlugin.platform}.")
        }
    }
}
