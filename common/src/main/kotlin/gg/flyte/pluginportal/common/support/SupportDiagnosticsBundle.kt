package gg.flyte.pluginportal.common.support

import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.HashType
import java.io.File
import java.nio.file.Paths
import java.util.zip.ZipFile

data class SupportBundleSection(
    val name: String,
    val contentType: String,
    val content: String,
    val truncated: Boolean = false
)

data class SupportDiagnosticsPayload(
    val payloadVersion: Int = 1,
    val pluginVersion: String,
    val serverVersion: String,
    val minecraftVersion: String,
    val serverName: String,
    val generatedAt: String,
    val sections: List<SupportBundleSection>,
    val redactionSummary: Map<String, Int>
)

object SupportDiagnosticsBundle {
    private const val MAX_SECTION_CHARS = 1_500_000
    private val plugin get() = PluginPortalBase.plugin

    fun build(): SupportDiagnosticsPayload {
        val redactions = mutableMapOf<String, Int>()
        val sections = mutableListOf<SupportBundleSection>()

        sections.addJson("environment", environment(), redactions)
        sections.addJson("server-plugins", serverPlugins(), redactions)
        sections.addJson("plugin-folder", pluginFolderInventory(), redactions)
        sections.addJson("plugin-portal-cache", LocalPluginCache.toList(), redactions)
        sections.addFile("plugin-portal-plugins-json", File(plugin.dataFolder, "plugins.json"), redactions)
        sections.addFile("plugin-portal-config", File(plugin.dataFolder, "config.yml"), redactions)
        sections.addFile("plugin-portal-history", File(plugin.dataFolder, "history.log"), redactions)
        sections.addText("plugin-portal-history-archives", archivedHistory(), redactions)
        sections.addFile("minecraft-latest-log", Paths.get("./logs/latest.log").toFile(), redactions)

        return SupportDiagnosticsPayload(
            pluginVersion = plugin.description.version,
            serverVersion = plugin.server.version,
            minecraftVersion = plugin.server.bukkitVersion,
            serverName = plugin.server.name,
            generatedAt = java.time.Instant.now().toString(),
            sections = sections,
            redactionSummary = redactions.toSortedMap()
        )
    }

    private fun MutableList<SupportBundleSection>.addJson(name: String, value: Any, redactions: MutableMap<String, Int>) {
        addText(name, GSON.toJson(value), redactions, "json")
    }

    private fun MutableList<SupportBundleSection>.addFile(name: String, file: File, redactions: MutableMap<String, Int>) {
        val text = if (file.exists() && file.isFile) file.readText() else "${file.path} not found"
        addText(name, text, redactions)
    }

    private fun MutableList<SupportBundleSection>.addText(
        name: String,
        rawText: String,
        redactions: MutableMap<String, Int>,
        contentType: String = "text"
    ) {
        val sanitized = SupportSanitizer.sanitize(rawText, redactions)
        val truncated = sanitized.length > MAX_SECTION_CHARS
        add(
            SupportBundleSection(
                name = name,
                contentType = contentType,
                content = if (truncated) sanitized.take(MAX_SECTION_CHARS) else sanitized,
                truncated = truncated
            )
        )
    }

    private fun environment(): Map<String, Any?> {
        return mapOf(
            "pluginPortalVersion" to plugin.description.version,
            "pluginPortalArtifact" to "PluginPortal",
            "premiumEntitled" to PluginPortalBase.info.hasPremiumEntitlement(),
            "serverName" to plugin.server.name,
            "serverVersion" to plugin.server.version,
            "minecraftVersion" to plugin.server.bukkitVersion,
            "onlineMode" to plugin.server.onlineMode,
            "managedPlugins" to LocalPluginCache.size,
            "serverPlugins" to plugin.server.pluginManager.plugins.size,
            "javaVersion" to System.getProperty("java.version"),
            "osName" to System.getProperty("os.name"),
            "osArch" to System.getProperty("os.arch"),
            "availableProcessors" to Runtime.getRuntime().availableProcessors(),
            "maxMemoryBytes" to Runtime.getRuntime().maxMemory(),
            "freeMemoryBytes" to Runtime.getRuntime().freeMemory()
        )
    }

    private fun serverPlugins(): List<Map<String, Any?>> {
        return plugin.server.pluginManager.plugins
            .sortedBy { it.name.lowercase() }
            .map {
                mapOf(
                    "name" to it.name,
                    "version" to it.description.version,
                    "enabled" to it.isEnabled,
                    "authors" to it.description.authors
                )
            }
    }

    private fun pluginFolderInventory(): List<Map<String, Any?>> {
        val folders = listOf(File("plugins"), File("plugins/update"))
        return folders.flatMap { folder ->
            folder.listFiles()?.toList().orEmpty().map { file ->
                val sha256 = if (file.isFile && file.extension.equals("jar", ignoreCase = true)) {
                    runCatching { HashType.SHA256.hash(file) }.getOrNull()
                } else null
                val managed = sha256?.let { hash -> LocalPluginCache.find { it.sha256 == hash } }

                mapOf(
                    "folder" to folder.path,
                    "name" to file.name,
                    "isFile" to file.isFile,
                    "extension" to file.extension,
                    "sizeBytes" to if (file.isFile) file.length() else null,
                    "lastModified" to java.time.Instant.ofEpochMilli(file.lastModified()).toString(),
                    "sha256" to sha256,
                    "managedPlugin" to managed?.let { "${it.name} (${it.platform} ${it.platformId})" }
                )
            }
        }
    }

    private fun archivedHistory(): String {
        val archiveFolder = File(plugin.dataFolder, "archive")
        return archiveFolder.listFiles { file -> file.extension == "zip" }
            ?.sortedByDescending { it.lastModified() }
            ?.take(5)
            ?.joinToString("\n\n") { file ->
                runCatching {
                    ZipFile(file).use { zip ->
                        zip.entries().asSequence().joinToString("\n") { entry ->
                            "== ${file.name}/${entry.name} ==\n" +
                                zip.getInputStream(entry).bufferedReader().readText()
                        }
                    }
                }.getOrElse { "Failed to read ${file.name}: ${it.message ?: it::class.simpleName}" }
            }
            ?: "No archived Plugin Portal history logs"
    }
}
