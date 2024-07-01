package gg.flyte.pluginportal.plugin.manager

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
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
import java.util.*
import java.util.concurrent.TimeUnit

object MarketplacePluginCache : PluginCache<Plugin>() {

    private val pluginCache: LoadingCache<Pair<MarketplacePlatform, String>, Optional<Plugin>> = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build(
            object : CacheLoader<Pair<MarketplacePlatform, String>, Optional<Plugin>>() {
                override fun load(key: Pair<MarketplacePlatform, String>): Optional<Plugin> {
                    return Optional.ofNullable(API.getPlugin(key.first, key.second))
                }
            }
        )

    fun getFilteredPlugins(
        prefix: String,
        platform: MarketplacePlatform? = null,
    ): List<Plugin> {
        var plugins = API.getPlugins(prefix)
        if (platform != null) plugins = plugins.filter { it.platforms.containsKey(platform) }
        return plugins
    }

    fun getPluginById(platform: MarketplacePlatform, platformId: String): Plugin? {
        return pluginCache.get(platform to platformId).orElse(null)
    }

    fun handlePluginSearchFeedback(
        audience: Audience,
        name: String,
        platform: MarketplacePlatform?,
        nameIsId: Boolean,
        ifSingle: (Plugin) -> Unit,
        ifMore: (List<Plugin>) -> Unit
    ) = async {
        when {
            nameIsId -> {
                if (platform == null) {
                    audience.sendFailure("Specify a platform to check the platformId: $name")
                } else {
                    getPluginById(platform, name)?.let { ifSingle(it) }
                        ?: audience.sendFailure("No plugin found")
                }
            }
            else -> {
                val plugins = getFilteredPlugins(name, platform)
                when {
                    plugins.isEmpty() -> audience.sendFailure("No plugins found")
                    plugins.size == 1 -> ifSingle(plugins.first())
                    else -> ifMore(plugins.sortedByRelevance(name))
                }
            }
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