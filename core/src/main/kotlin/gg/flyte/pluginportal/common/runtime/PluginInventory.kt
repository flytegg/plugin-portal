package gg.flyte.pluginportal.common.runtime

import com.google.gson.JsonObject
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.util.HashType
import gg.flyte.pluginportal.common.util.getPluginYML
import java.io.File

/** Reads the installed and pending JARs without contacting a marketplace. Owned by one JVM runtime. */
class PluginInventory(private val runtime: PortalRuntime) {
    data class Entry(
        val name: String,
        val version: String,
        val runningVersion: String?,
        val state: String,
        val stagedVersion: String? = null,
        val managed: LocalPlugin? = null,
    ) {
        fun json() = JsonObject().apply {
            addProperty("name", name.take(120))
            addProperty("version", version.take(120))
            addProperty("state", state)
            runningVersion?.let { addProperty("runningVersion", it.take(120)) }
            stagedVersion?.let { addProperty("stagedVersion", it.take(120)) }
            managed?.let {
                addProperty("platform", it.platform.name)
                addProperty("id", it.platformId.take(120))
            }
        }
    }
    data class Snapshot(val plugins: List<Entry>, val restartCount: Int) {
        val restartRequired get() = restartCount > 0
    }
    private data class Jar(val file: File, val names: Set<String>, val version: String?, val hash: String?)
    private data class CachedJar(val modified: Long, val size: Long, val jar: Jar)
    private data class Removal(val plugin: LocalPlugin, val names: Set<String>)
    private val jars = mutableMapOf<File, CachedJar>()
    private val removals = mutableMapOf<Pair<String, String>, Removal>()

    /** Capture descriptor names before deletion, since catalog titles can differ from runtime names. */
    @Synchronized fun recordRemoval(plugin: LocalPlugin, files: List<File>) {
        removals[plugin.platform.name to plugin.platformId] = Removal(plugin,
            files.flatMap { readJar(it).names }.toSet() + plugin.name.lowercase())
    }

    @Synchronized fun snapshot(managed: List<LocalPlugin> = LocalPluginCache.toList()): Snapshot {
        val loaded = runtime.server.plugins
        val pendingFiles = runtime.updateDirectory.listFiles { file -> file.isFile }.orEmpty().toList()
        val installed = runtime.installDirectory.listFiles { file -> file.isFile && file.extension.equals("jar", true) }
            .orEmpty().map(::readJar)
        val staged = pendingFiles.filter { it.extension.equals("jar", true) }.map(::readJar)
        val allJars = installed + staged
        jars.keys.retainAll(allJars.map { it.file }.toSet())
        val usedLoaded = mutableSetOf<InstalledPlugin>()
        val usedPending = mutableSetOf<File>()
        val entries = mutableListOf<Entry>()
        for (plugin in managed) {
            val anchor = allJars.firstOrNull { it.hash != null && it.hash.equals(plugin.sha256, true) }
            val names = anchor?.names.orEmpty() + plugin.name.lowercase()
            val running = loaded.firstOrNull { it.name.lowercase() in names }
            val replacement = staged.firstOrNull { it.names.any(names::contains) }
            val disk = installed.firstOrNull { it.names.any(names::contains) }
            // A stale cache alone is not evidence of an install waiting on disk.
            if (running == null && disk == null && replacement == null) continue
            running?.let(usedLoaded::add)
            replacement?.let { usedPending += it.file }
            val changedVersion = running != null && (plugin.version != running.description.version ||
                (disk?.version != null && disk.version != running.description.version))
            val state = when {
                running == null -> "staged-install"
                replacement != null || changedVersion -> "staged-update"
                else -> "running"
            }
            val stagedVersion = if (state == "staged-update") replacement?.version ?: disk?.version?.takeIf { it != running?.description?.version } ?: plugin.version else null
            entries += Entry(plugin.name, plugin.version, running?.description?.version, state, stagedVersion, plugin)
        }
        for (removal in removals.values) {
            val running = loaded.firstOrNull { it !in usedLoaded && it.name.lowercase() in removal.names } ?: continue
            // Reinstalling the JAR in this session cancels the pending removal.
            if (allJars.any { it.names.any(removal.names::contains) }) continue
            usedLoaded += running
            entries += Entry(removal.plugin.name, removal.plugin.version, running.description.version,
                "staged-removal", managed = removal.plugin)
        }
        for (running in loaded.filterNot(usedLoaded::contains)) {
            val replacement = staged.firstOrNull { running.name.lowercase() in it.names }
            replacement?.let { usedPending += it.file }
            entries += Entry(running.name, running.description.version, running.description.version,
                if (replacement == null) "running" else "staged-update", replacement?.version)
        }
        // Count unrecognized/broken pending files too, without double-counting matched replacements.
        val count = entries.count { it.state != "running" } + pendingFiles.count { it !in usedPending }
        return Snapshot(entries, count)
    }

    private fun readJar(file: File): Jar {
        val modified = file.lastModified()
        val size = file.length()
        jars[file]?.takeIf { it.modified == modified && it.size == size }?.let { return it.jar }
        val descriptor = file.getPluginYML()
        val names = listOfNotNull(descriptor?.get("name")?.toString(), descriptor?.get("id")?.toString())
            .map(String::lowercase).toSet()
        val jar = Jar(file, names, descriptor?.get("version")?.toString(), runCatching { HashType.SHA256.hash(file) }.getOrNull())
        jars[file] = CachedJar(modified, size, jar)
        return jar
    }
}
