package gg.flyte.pluginportal.common.managers

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.PlatformId
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.chat.sendFailure
import gg.flyte.pluginportal.common.logging.PortalLogger
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.*
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.time.Duration
import java.time.temporal.ChronoUnit

object MarketplacePluginCache: PluginCache<Plugin>() {

    private val pp: JavaPlugin get() = PluginPortalBase.plugin

    private val pluginCache: Cache<PlatformId, Plugin> = CacheBuilder.newBuilder().maximumSize(10_000).expireAfterWrite(Duration.of(2, ChronoUnit.HOURS)).build()

    private var timer: CancellableTimer<*>? = null

    fun startCacheLoader() {
        if (timer != null) return
        timer = asyncTimer(20 * 60 * 60, 0) { loadLocalPluginData() }
    }

    fun stopCacheLoader() {
        timer?.cancel()
        timer = null
    }

    private fun loadLocalPluginData() {
        // TODO: 'XX plugins need an update' message would go here or at least that info would be loaded here
        val plugins = API.fetchLocalPluginRemotes() ?: return pp.logger.warning("Failed to fetch installed plugin data from server")
        if (plugins.isEmpty()) return // unlucky

        plugins.forEach { (entryid, plugin) ->
            if (plugin == null) return@forEach pp.logger.severe("Failed to load plugin with EID: $entryid")
            val platformId = plugin.platforms.byEntryId(entryid)?.platformWithId ?: return@forEach pp.logger.severe("Failed to load platform with EID $entryid")
            putPlugin(plugin, platformId)
        }
    }

    fun getFilteredPlugins(prefix: String, platform: MarketplacePlatform? = null): List<Plugin> = API.getPlugins(prefix, platform = platform)?.toList() ?: listOf()
    fun getCachedPluginById(platform: MarketplacePlatform, platformId: String): Plugin? = pluginCache.getIfPresent(PlatformId(platformId, platform))
    fun getOrFetchPluginById(platform: MarketplacePlatform, platformId: String): Plugin? {
        val hit = getCachedPluginById(platform, platformId)
        if (hit != null) return hit
        val lookupId = PlatformId(platformId, platform)
        val remote = API.getPluginByPlatformId(lookupId) ?: findPluginByPlatformQuery(platform, platformId)
        if (remote == null) {
            pp.logger.warning("MarketplaceCache failed to fetch ($platform $platformId)")
            return null
        }
        putPlugin(remote, lookupId)
        return remote
    }

    private fun findPluginByPlatformQuery(platform: MarketplacePlatform, query: String): Plugin? {
        val normalizedQuery = query.normalizedLookup()
        return getFilteredPlugins(query, platform)
            .firstOrNull { plugin ->
                plugin.name.normalizedLookup() == normalizedQuery ||
                    plugin.platform(platform)?.platformId?.normalizedLookup() == normalizedQuery
            }
    }

    fun putPlugin(plugin: Plugin, primaryPlatformId: PlatformId? = null) {
        primaryPlatformId?.let { pluginCache.put(it, plugin) }
        plugin.platforms.asList().forEach { platformPlugin ->
            pluginCache.put(platformPlugin.platformWithId, plugin)
        }
    }

    /** Exclusively designed for usage by the RecognizeAll Command. */
    fun addRecognizeAllPluginsToCache(newPlugins: List<LocalPlugin>) {
        val plugins = API.getAllPluginsByPlatformIds(newPlugins.map(LocalPlugin::platformWithId))
            ?: return logger.severe("Failed to fetch newly recognized plugins, they cannot be added to the cache.")

        plugins.keys.forEach { platform ->
            plugins[platform]!!.values.forEach { plugin ->
                val pid = plugin?.platform(platform)?.platformWithId ?: return@forEach
                putPlugin(plugin, pid)
            }
        }
    }

    fun handlePluginSearchFeedback(
        audience: Audience,
        name: String,
        platform: MarketplacePlatform?,
        nameIsId: Boolean,
        ifSingle: (Plugin) -> Unit,
        ifMore: (List<Plugin>) -> Unit,
        exact: Boolean = false
    ) = async {
        when {
            nameIsId -> {
                if (platform == null) {
                    audience.sendFailure("Specify a platform to check the platformId: $name")
                } else {
                   getOrFetchPluginById(platform, name)?.let { ifSingle(it) } ?: audience.sendFailure("No plugin found")
                }
            }

            else -> {
                var plugins = getFilteredPlugins(name, platform)
                if (exact) {
                    plugins = plugins.filter { it.name.equals(name, true) }
                }
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
        targetDirectory: File,
    ): ActionResponse<LocalPlugin> {
        if (!Config.isDownloadPlatformEnabled(platform)) {
            return ActionResponseString(false, "Downloading from ${platform.name} is disabled in config.yml")
        }

        val platformPlugin = plugin.platform(platform) ?: return ActionResponseString(false, "No platform plugin found")
        val latestVersion = platformPlugin.newestCompatibleVersion(null, currentServerTypePreference(), currentMinecraftVersion())
            ?: return ActionResponseString(false, "No compatible version found for platform ${platform.name}")

        val downloadURL = latestVersion.downloadURL ?: return ActionResponseString(false, "No download URL found for platform ${platform.name}")

        if (!isValidDownload(downloadURL)) {
            val errorMsg = when {
                downloadURL.contains("/versions") || downloadURL.contains("/releases") ->
                    "This plugin uses an external download page instead of a direct download. Please download it manually from: $downloadURL"

                downloadURL.contains("github.com") && !downloadURL.contains("/download") ->
                    "This appears to be a GitHub page, not a direct download link. Please use the releases download URL instead."

                else ->
                    "Invalid download URL. The URL does not appear to lead to a JAR file: $downloadURL"
            }
            return ActionResponseString(false, errorMsg)
        }

        val targetMessage = "${plugin.name} from ${platform.name} with ID ${plugin.id}"
        PortalLogger.log(audience, PortalLogger.Action.INITIATED_INSTALL, targetMessage)

        val newPlugin = plugin.download(false, platform, audience)
        return if (newPlugin != null) {
            PortalLogger.log(audience, PortalLogger.Action.INSTALL, targetMessage)
            ActionResponseString(true, "Downloaded ${plugin.name} from ${platform.name}.", newPlugin)
        } else {
            PortalLogger.log(audience, PortalLogger.Action.FAILED_INSTALL, targetMessage)
            ActionResponseString(false, "Direct download failed")
        }
    }

    /**
     * Sorts the plugins by relevance to the query, this also takes downloads etc. into account
     */
    fun List<Plugin>.sortedByRelevance(query: String): List<Plugin> = sortedByDescending {
        it.totalDownloads * if (query.equals(it.name, true)) 50 else 1 // Arbitrary bias to exact matches
    }

    private fun String.normalizedLookup(): String =
        lowercase().filter { it.isLetterOrDigit() }
}
