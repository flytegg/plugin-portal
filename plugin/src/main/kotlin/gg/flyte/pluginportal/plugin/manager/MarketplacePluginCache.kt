package gg.flyte.pluginportal.plugin.manager

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.chat.*
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.util.async
import gg.flyte.pluginportal.plugin.util.download
import gg.flyte.pluginportal.plugin.util.isValidDownload
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor

object MarketplacePluginCache : PluginCache<Plugin>() {

    fun getFilteredPlugins(
        prefix: String,
        platform: MarketplacePlatform? = null,
    ): List<Plugin> {
        var plugins = API.getPlugins(prefix)
        if (platform != null) plugins = plugins.filter { it.platforms.containsKey(platform) }
        return plugins
    }

    fun getPluginById(platform: MarketplacePlatform, platformId: String) = API.getPlugin(platform, platformId) // TODO: Add a caching layer here

    fun handlePluginSearchFeedback (
        audience: Audience,
        name: String,
        platform: MarketplacePlatform?,
        nameIsId: Boolean,
        ifSingle: (Plugin) -> Unit, // Async
        ifMore: (List<Plugin>) -> Unit // Also Async
    ) {
        val prefix = if (nameIsId) null else name
        val id = if (nameIsId) name else null

        if (id != null) {
            if (platform == null) return audience.sendFailure("Specify a platform to check the platformId: $id")
            return async {
                getPluginById(platform, id)?.let { ifSingle.invoke(it) } ?: audience.sendFailure("No plugin found")
            }
        }

        async {
            val plugins = getFilteredPlugins(prefix!!, platform) // May not return all results

            if (plugins.isEmpty()) return@async audience.sendFailure("No plugins found")

            if (plugins.size == 1) ifSingle.invoke(plugins.first())
            else ifMore.invoke(plugins.sortedByRelevance(name))
        }
    }

    fun installPlugin(
        audience: Audience,
        plugin: Plugin,
        platform: MarketplacePlatform,
        targetDirectory: String,
    ) {
        val platformPlugin =
            plugin.platforms[platform] ?: return sendFailureMessage(audience, "No platform plugin found")

        val downloadURL = platformPlugin.download?.url
            ?: return audience.sendMessage(
                text("No download URL found for platform ", NamedTextColor.RED)
                    .append(text(platform.name, NamedTextColor.AQUA))
                    .append(endLine())
            )

        if (!isValidDownload(downloadURL)) {
            return audience.sendMessage(
                status(Status.FAILURE, "Invalid download URL. See more information below")
                    .appendNewline()
                    .append(
                        textSecondary(
                            """
                        - The URL must be a direct download link
                        - The URL must download a JAR file
                        - Please contact us in our Discord for more information
                            """.trimIndent()
                        )
                    )
                    .append(endLine())
            )
        }

        audience.sendMessage(
            textSecondary("Found download URL, starting installation from: ")
                .appendPrimary(platform.name).appendSecondary("...")
        )


        val targetMessage = "${plugin.name} from ${platform.name} with ID ${plugin.id}"
        PortalLogger.log(audience, PortalLogger.Action.INITIATED_INSTALL, targetMessage)

        if (plugin.download(platform, targetDirectory, audience))
            PortalLogger.log(audience, PortalLogger.Action.INSTALL, targetMessage)
        else
            return PortalLogger.log(audience, PortalLogger.Action.FAILED_INSTALL, targetMessage)

        audience.sendMessage(
            newline()
                .appendStatus(Status.SUCCESS, "Downloaded ${plugin.name} from ${platform.name}.\n")
                .appendSecondary("- Please restart your server to enable this plugin")
                .append(endLine())
        )
    }

    /**
     * Sorts the plugins by relevance to the query, this also takes downloads etc. into accoutnt
     */
    fun List<Plugin>.sortedByRelevance(query: String): List<Plugin> = sortedByDescending {
        it.totalDownloads * if (query.equals(it.name, true)) 50 else 1 // Arbitrary bias to exact matches
    }
}