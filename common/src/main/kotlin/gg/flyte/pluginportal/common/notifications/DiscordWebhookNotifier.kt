package gg.flyte.pluginportal.common.notifications

import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.Version
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.async
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object DiscordWebhookNotifier {
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json".toMediaType()
    private const val USERNAME = "Plugin Portal"
    private const val AVATAR_URL = "https://api.cdn.wip.group/K-duNyUe.png"
    private const val INSTALL_COLOR = 0x22C55E
    private const val UPDATE_COLOR = 0xF59E0B
    private const val UNINSTALL_COLOR = 0xEF4444
    private const val PLATFORM_SWITCH_COLOR = 0xA855F7
    private const val PLUGIN_PORTAL_UPDATE_COLOR = 0x38BDF8

    fun managedPluginInstalled(plugin: LocalPlugin, link: String? = null) {
        send(
            title = "Plugin installed",
            description = "${plugin.name} ${plugin.version} was installed from ${plugin.platform.name}.",
            color = INSTALL_COLOR,
            fields = listOf(
                "Plugin" to plugin.name,
                "Version" to plugin.version,
                "Platform" to plugin.platform.name,
                "Platform ID" to plugin.platformId,
                "Channel" to (plugin.preferredChannel ?: "default"),
            ).withOptional("Link", link)
        )
    }

    fun managedPluginUpdated(previous: LocalPlugin, current: LocalPlugin, version: Version? = null, link: String? = null) {
        send(
            title = "Plugin updated",
            description = "${current.name} was updated from ${previous.version} to ${current.version}.",
            color = UPDATE_COLOR,
            fields = listOf(
                "Plugin" to current.name,
                "Version" to "${previous.version} -> ${current.version}",
                "Platform" to current.platform.name,
                "Platform ID" to current.platformId,
                "Channel" to (current.preferredChannel ?: "default"),
            ).withOptional("Link", version?.releaseUrl(link) ?: link)
        )
    }

    fun managedPluginUninstalled(plugin: LocalPlugin, link: String? = null) {
        send(
            title = "Plugin uninstalled",
            description = "${plugin.name} ${plugin.version} was uninstalled.",
            color = UNINSTALL_COLOR,
            fields = listOf(
                "Plugin" to plugin.name,
                "Version" to plugin.version,
                "Platform" to plugin.platform.name,
                "Platform ID" to plugin.platformId,
            ).withOptional("Link", link)
        )
    }

    fun managedPluginPlatformSwitched(previous: LocalPlugin, current: LocalPlugin, link: String? = null) {
        send(
            title = "Plugin platform switched",
            description = "${current.name} now tracks ${current.platform.name}.",
            color = PLATFORM_SWITCH_COLOR,
            fields = listOf(
                "Plugin" to current.name,
                "Version" to current.version,
                "Platform" to "${previous.platform.name} -> ${current.platform.name}",
                "Platform ID" to current.platformId,
                "Channel" to (current.preferredChannel ?: "default"),
            ).withOptional("Link", link)
        )
    }

    fun pluginPortalUpdated(previousVersion: String, currentVersion: String, automatic: Boolean, changelog: String? = null) {
        val releaseNotes = changelog?.lineSequence()
            ?.filter { it.isNotBlank() }
            ?.take(4)
            ?.joinToString("\n")

        send(
            title = if (automatic) "Plugin Portal auto-update downloaded" else "Plugin Portal update downloaded",
            description = "Plugin Portal $currentVersion was downloaded and will apply after restart.",
            color = PLUGIN_PORTAL_UPDATE_COLOR,
            fields = listOf(
                "Version" to "$previousVersion -> $currentVersion",
                "Artifact" to "PluginPortal",
                "Premium" to if (PluginPortalBase.info.hasPremiumEntitlement()) "Unlocked" else "Locked",
                "Mode" to if (automatic) "Automatic" else "Manual",
            ).withOptional("Release notes", releaseNotes)
        )
    }

    fun close() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
        client.cache?.close()
    }

    private fun send(title: String, description: String, color: Int, fields: List<Pair<String, String>>) {
        val webhookUrl = Config.getDiscordWebhookUrl() ?: return
        if (!webhookUrl.startsWith("https://discord.com/api/webhooks/") && !webhookUrl.startsWith("https://discordapp.com/api/webhooks/")) {
            PluginPortalBase.plugin.logger.warning("Discord webhook notification skipped: configured webhook URL is invalid.")
            return
        }

        async {
            runCatching {
                val body = GSON.toJson(
                    mapOf(
                        "username" to USERNAME,
                        "avatar_url" to AVATAR_URL,
                        "embeds" to listOf(
                            mapOf(
                                "title" to title,
                                "description" to description,
                                "color" to color,
                                "fields" to fields.plus("Plugin Portal" to PluginPortalBase.plugin.description.version).map { (name, value) ->
                                    mapOf(
                                        "name" to name,
                                        "value" to value.take(1024),
                                        "inline" to true
                                    )
                                }
                            )
                        )
                    )
                )

                val request = Request.Builder()
                    .url(webhookUrl)
                    .post(body.toRequestBody(jsonMediaType))
                    .header("User-Agent", "PluginPortal/1.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        PluginPortalBase.plugin.logger.warning("Discord webhook notification failed with HTTP ${response.code}.")
                    }
                }
            }.onFailure {
                PluginPortalBase.plugin.logger.warning("Discord webhook notification failed: ${it.message ?: it::class.simpleName}.")
            }
        }
    }

    private fun Version.releaseUrl(platformUrl: String?): String? {
        val downloadUrl = downloadURL
        if (platformUrl != null && downloadUrl != null && platformUrl.contains("modrinth.com")) {
            val versionId = Regex("/versions/([^/]+)/").find(downloadUrl)?.groupValues?.getOrNull(1)
            if (!versionId.isNullOrBlank()) return "$platformUrl/version/$versionId"
        }
        return platformUrl
    }

    private fun List<Pair<String, String>>.withOptional(name: String, value: String?): List<Pair<String, String>> =
        if (value.isNullOrBlank()) this else plus(name to value)
}
