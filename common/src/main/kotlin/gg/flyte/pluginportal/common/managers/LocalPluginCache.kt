package gg.flyte.pluginportal.common.managers

import com.google.gson.JsonSyntaxException
import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.Constants
import gg.flyte.pluginportal.common.EntryIdMap
import gg.flyte.pluginportal.common.PlatformId
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.chat.sendFailure
import gg.flyte.pluginportal.common.logging.PortalLogger
import gg.flyte.pluginportal.common.notifications.DiscordWebhookNotifier
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform.*
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.types.Version
import gg.flyte.pluginportal.common.util.*
import net.kyori.adventure.audience.Audience
import java.io.File
import java.nio.file.Files

object LocalPluginCache : PluginCache<LocalPlugin>() {
    data class FailedPluginDelete(val file: File, val reason: String)
    private val unsafeDownloadNameChars = Regex("[/\\\\]")

    fun hasPlugin(plugin: Plugin) = any(plugin::isParentOf)
    fun hasPlugin(id: String) = any { it.platformId == id }
    fun hasPlugin(platformId: String, platform: MarketplacePlatform) = any { it.platform == platform && it.platformId == platformId }
    fun hasPluginByHash(sha256: String) = any { it.sha256 == sha256 }
    fun hasManagedDownloadedFile(file: File) = any { plugin ->
        val downloadedName = plugin.name.replace(unsafeDownloadNameChars, "")
        file.name == "[PP] $downloadedName (${plugin.platform}).jar"
    }

    fun fromPlugin(plugin: Plugin): LocalPlugin? = find(plugin::isParentOf)

    fun getAllPluginsAsPlatformIds() = map { PlatformId(it.platformId, it.platform) }
    fun getEntryIdMap(): EntryIdMap = groupBy { it.platform }
        .mapValues { it.value.map(LocalPlugin::entryId) }
        .let { EntryIdMap(it[MODRINTH] ?: listOf(), it[HANGAR] ?: listOf(), it[SPIGOTMC] ?: listOf(), it[POLYMART] ?: listOf()) }

    fun reloadCache() {
        val text = getPluginsFile().readText()
        if (text.isEmpty()) return

        try {
            clear()
            val plugins = GSON.fromJson(text, Array<LocalPlugin>::class.java)
            addAll(plugins)

        } catch (_: JsonSyntaxException) {
        }
    }



    fun load() {
        val ppLocalPlugin = LocalPlugin(
            entryId = "68813756d1528bfc8a6782f6",
            platformId = "5qkQnnWO",
            name = "PluginPortal",
            version = PluginPortalBase.plugin.description.version,
            platform = MarketplacePlatform.MODRINTH,
            sha256 = HashType.SHA256.hash(PluginPortalBase.info.pluginJarFile),
            sha512 = HashType.SHA512.hash(PluginPortalBase.info.pluginJarFile),
            installedAt = System.currentTimeMillis(),
        )

        val pluginsInFolder: Map<String, File> = Constants.INSTALL_DIRECTORY
            .listFiles()
            ?.filter(File::isJarFile)
            ?.associateBy { HashType.SHA256.hash(it) }
            ?: mapOf()

        val text = getPluginsFile().readText()
        // If the config is empty, add the local pp plugin
        if (text.isEmpty()) {
            add(ppLocalPlugin)
            return
        }

        try {
            val plugins = GSON.fromJson(text, Array<LocalPlugin>::class.java)

            plugins.forEach { plugin ->
                // Update Plugin Portal's own local cache entry.
                if (plugin.isPluginPortal) {
                    add(ppLocalPlugin)
                // Plugin no longer present in plugins folder upon opening server
                } else if (!pluginsInFolder.containsKey(plugin.sha256)) {
                    PortalLogger.info(PortalLogger.Action.NOTICED_DELETE, "of ${plugin.name} (${plugin.platformId}) while Plugin Portal was Disabled.")
                // BAU
                } else {
                    add(plugin)
                }
            }

            if (none(LocalPlugin::isPluginPortal)) add(ppLocalPlugin)

            save()

            PortalLogger.info(PortalLogger.Action.LOAD_PLUGINS, "Loaded ${plugins.size} plugins from local cache")
        } catch (_: JsonSyntaxException) {
        }
    }

    fun save() {
        async {
            val text = GSON.toJson(toTypedArray().distinctBy { plugin -> plugin.entryId })
            getPluginsFile().writeText(text)
        }
    }


    fun deletePlugin(plugin: LocalPlugin, toDelete: List<File?>): List<FailedPluginDelete> {
        val failedDeletes = toDelete
            .filterNotNull()
            .mapNotNull { file ->
                try {
                    Files.deleteIfExists(file.toPath())
                    null
                } catch (e: Exception) {
                    FailedPluginDelete(file, e.message ?: e.javaClass.simpleName)
                }
            }

        if (failedDeletes.isNotEmpty()) return failedDeletes

        remove(plugin)
        save()
        return emptyList()
    }

    fun LocalPlugin.installUpdate(
        initiator: Audience,
        ignoreOutdated: Boolean = false,
        marketplacePlugin: Plugin? = null,
        targetVersionOverride: Version? = null,
        preferredChannelOverride: String? = null,
        excludedFromUpdatesOverride: Boolean = excludedFromUpdates,
    ): ActionResponse<LocalPlugin> {
        val target = "$name with ID $platformId on $platform"
        val marketplacePlugin: Plugin = marketplacePlugin
            ?: MarketplacePluginCache.getCachedPluginById(platform, platformId)
            ?: return run { // They possibly manually changed ID or our database down?
                ServerTelemetryManager.recordManagedPluginUpdateFailed()
                PortalLogger.log(initiator, PortalLogger.Action.FAILED_UPDATE, target)
                ActionResponseString(false, "Could not find plugin in marketplace ($target)")
            }

        val targetVersion = targetVersionOverride ?: targetUpdateVersion(marketplacePlugin)
            ?: return ActionResponseString(false, "No compatible version found for ${preferredChannel ?: "the default channel"}")

        if (!ignoreOutdated && matchesVersion(targetVersion)) return ActionResponseString(false, "Plugin is already up to date")

        val newPlugin = marketplacePlugin.download(
            update = true,
            marketplacePlatform = platform,
            audience = initiator,
            version = targetVersion,
            preferredChannel = preferredChannelOverride ?: preferredChannel ?: targetVersion.releaseChannel,
            excludedFromUpdates = excludedFromUpdatesOverride,
        )
//        val response = MarketplacePluginCache.installPlugin(initiator, marketplacePlugin, platform, Constants.UPDATE_DIRECTORY)

        if (newPlugin != null) {
            remove(this) // From local plugin cache, as install adds the new version
            addToUpdatedPluginMap(newPlugin, this)
            save()
            PortalLogger.log(initiator, PortalLogger.Action.UPDATE, target)
            DiscordWebhookNotifier.managedPluginUpdated(
                this,
                newPlugin,
                targetVersion,
                marketplacePlugin.platform(platform)?.webpageURL,
            )
            return ActionResponseString<LocalPlugin>(true, null, newPlugin)
        } else {
            ServerTelemetryManager.recordManagedPluginUpdateFailed()
            PortalLogger.log(initiator, PortalLogger.Action.FAILED_UPDATE, target)
            return ActionResponseString<LocalPlugin>(false, "Plugin failed to update", null)
        }

    }

    private val updatedPluginMap: HashMap<LocalPlugin, File?> = hashMapOf()
    fun addToUpdatedPluginMap(newPlugin: LocalPlugin, oldPlugin: LocalPlugin) { updatedPluginMap[newPlugin] = oldPlugin.findFile() }
    /**
     * @return The file for the currently installed version of this plugin, if this [LocalPlugin] was installed via an update,
     *             otherwise null
     */
    fun LocalPlugin.popCurrentVersionFile() = updatedPluginMap.remove(this)

    private val pluginsFolder = File("plugins")
    private val updateFolder = File(pluginsFolder, "update").apply { if (!exists()) mkdirs() }

    fun LocalPlugin.findFile(): File? {
        val files = mutableListOf<File>().apply {
            addAll(pluginsFolder.listFiles() ?: emptyArray())
            addAll(updateFolder.listFiles() ?: emptyArray())
        }

        return files.filter { file -> file.isFile }
            .filter { file -> file.name.endsWith(".jar") }
            .firstOrNull { file -> HashType.SHA256.hash(file) == sha256 }
    }

    private fun getPluginsFile() = File(PluginPortalBase.plugin.dataFolder, "plugins.json").createIfNotExists()

    fun searchPluginsWithFeedback(
        audience: Audience,
        name: String,
        nameIsId: Boolean,
        ifSingle: (LocalPlugin) -> Unit, // Sync
        ifMore: (List<LocalPlugin>) -> Unit // Sync
    ) {
        val prefix = if (nameIsId) null else name
        val platformId = if (nameIsId) name else null

        val plugins = if (platformId != null) LocalPluginCache.find { it.platformId == platformId }?.let { listOf(it) }
            ?: listOf()
        else LocalPluginCache.filter { plugin -> plugin.name.startsWith(prefix ?: "", ignoreCase = true) }

        plugins.ifEmpty { return audience.sendFailure("No plugins found") }

        if (plugins.size == 1) ifSingle.invoke(plugins.first())
        else ifMore.invoke(plugins)
    }
}
