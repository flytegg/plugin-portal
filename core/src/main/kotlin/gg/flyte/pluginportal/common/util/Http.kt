package gg.flyte.pluginportal.common.util

import gg.flyte.pluginportal.common.AuthCreds
import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.Constants
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.notifications.DiscordWebhookNotifier
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.types.PolymartPlatformEntry
import gg.flyte.pluginportal.common.types.Version
import gg.flyte.pluginportal.common.types.newestCompatibleVersionWithFallback
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private fun Audience?.logFailure(msg: String, consolemsg: String) {
    this?.sendFailure(msg)
    if (this == null || !isConsole()) PluginPortalBase.plugin.logger.info(consolemsg)
}

fun Plugin.download(
    update: Boolean,
    marketplacePlatform: MarketplacePlatform?,
    audience: Audience? = null,
    version: Version? = null,
    preferredChannel: String? = null,
    excludedFromUpdates: Boolean = false,
): LocalPlugin? {

    val platformPlugin = if (marketplacePlatform != null) {
        platform(marketplacePlatform) ?: run {
            audience.logFailure(
                "$name is not available on ${marketplacePlatform.name}.",
                "Download blocked for $name ($id) because ${marketplacePlatform.name} is not available."
            )
            return null
        }
    } else {
        platforms.bestDownloadable ?: run {
            audience.logFailure(
                "No enabled downloadable platform is available for $name.",
                "Download blocked for $name ($id) because no enabled downloadable platform is available."
            )
            return null
        }
    }
    val platform = platformPlugin.platform
    if (!Config.isDownloadPlatformEnabled(platform)) {
        audience.logFailure(
            "Downloading from ${platform.name} is disabled in config.yml.",
            "Download blocked for $name ($platform ${platformPlugin.platformId}) because the platform is disabled."
        )
        return null
    }
    val targetDir = if (update) Constants.UPDATE_DIRECTORY else Constants.INSTALL_DIRECTORY
    val jarFile = File(targetDir, getFullDownloadedName(platform))
    val serverTypes = currentServerTypePreference()
    val minecraftVersion = currentMinecraftVersion()
    val version = version
        ?: platformPlugin.newestCompatibleVersionWithFallback(preferredChannel, serverTypes, minecraftVersion) {
            API.getPluginVersions(platformPlugin.platformWithId)?.toList()
        }
        ?: run {
            audience.logFailure(
                "No compatible version found for ${preferredChannel ?: "the default channel"}",
                "No compatible version found for $name ($platform ${platformPlugin.platformId})"
            )
            return null
        }

    if (!update && LocalPluginCache.hasPlugin(this)) {
        audience.logFailure("This plugin is already installed", "$name is already installed")
        return null
    }

    val downloadUrl = version.downloadURL ?: if (platform == MarketplacePlatform.POLYMART) {
        val polymart = platformPlugin as? PolymartPlatformEntry
        if (polymart?.premium != null) {
            audience.logFailure(
                "Downloading premium Polymart plugins is currently not supported.",
                "Unable to download premium Polymart plugin $name (${platformPlugin.platformId})"
            )
            return null
        }
        getPolymartDownloadUrl(platformPlugin.platformId)
    } else {
        null
    }

    if (downloadUrl == null) {
        audience.logFailure("""
            PluginPortal cannot determine a download URL for this plugin.
            This is usually because the plugin author is using an external download link not recognizable by plugin portal.
            """.trimIndent(), "Unable to determine a download URL for $name ($platform ${platformPlugin.platformId})" )
        return null
    }

    val file = download(
        URL(downloadUrl),
        jarFile,
        audience
    ) ?: return null

    LocalPluginCache.removeIf { plugin ->
        plugin.platformId == platformPlugin.platformId || plugin.entryId == platformPlugin.entryId || isParentOf(plugin)
    }

    val newPlugin = LocalPlugin(
        entryId = platformPlugin.entryId,
        platformId = platformPlugin.platformId,
        name = name,
        version = version.versionNumber,
        platform = platform,
        sha256 = HashType.SHA256.hash(file),
        sha512 = HashType.SHA512.hash(file),
        installedAt = System.currentTimeMillis(),
        preferredChannel = preferredChannel ?: version.releaseChannel,
        excludedFromUpdates = excludedFromUpdates,
    )

    LocalPluginCache.add(newPlugin)
    MarketplacePluginCache.putPlugin(this, newPlugin.platformWithId)

    LocalPluginCache.save()

    if (!update) {
        DiscordWebhookNotifier.managedPluginInstalled(newPlugin, platformPlugin.webpageURL)
    }

    return newPlugin
}

fun download(url: URL, destination: File, audience: Audience?, authCreds: AuthCreds? = null): File? = runCatching {
    val connection = url.openConnection().apply {
        connectTimeout = 15_000
        readTimeout = 60_000
        // Use a more specific User-Agent for Polymart compatibility
        setRequestProperty("User-Agent", "PluginPortal/1.0")
        val apiKey = authCreds?.mclKey?.trim()?.takeIf { it.isNotEmpty() }
        if (apiKey != null && (url.host == "localhost" || url.host == "pluginportal.link" || url.host.endsWith(".pluginportal.link"))) {
            setRequestProperty("x-api-key", apiKey)
            setRequestProperty("Authorization", "Bearer $apiKey")
        }
        // Handle redirects
        if (this is HttpURLConnection) {
            instanceFollowRedirects = true
        }
    }

    connection.getInputStream().use { input ->
        destination.parentFile.mkdirs()
        destination.outputStream().use { output -> input.copyTo(output) }
    }
    destination
}.onFailure {
    logger.warning("Download failed from $url: ${it.message ?: it::class.simpleName}")
    audience?.sendMessage(
        text("\n").append(
            status(Status.FAILURE, "An error occurred while downloading\n")
                .append(
                    textSecondary("- Please try again, or join our ")
                    .append(SharedComponents.DISCORD_COMPONENT)
                    .appendSecondary(" for support.")
                ).append(endLine())
        )
    )
}.getOrNull()
