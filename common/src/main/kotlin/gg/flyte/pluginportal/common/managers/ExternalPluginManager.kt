package gg.flyte.pluginportal.common.managers

import gg.flyte.pluginportal.common.Constants
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.adapters.external.ExternalArtifact
import gg.flyte.pluginportal.common.adapters.external.ExternalPluginConfig
import gg.flyte.pluginportal.common.adapters.external.ExternalPluginProvider
import gg.flyte.pluginportal.common.adapters.external.ExternalPluginState
import gg.flyte.pluginportal.common.adapters.external.ExternalUpdatePolicy
import gg.flyte.pluginportal.common.adapters.external.providers.GeyserExternalPluginProvider
import gg.flyte.pluginportal.common.adapters.external.providers.GitHubExternalPluginProvider
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.HashType
import gg.flyte.pluginportal.common.util.download
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.zip.ZipFile

object ExternalPluginManager {
    private val providers: Map<String, ExternalPluginProvider> = listOf(
        GitHubExternalPluginProvider(),
        GeyserExternalPluginProvider()
    ).associateBy(ExternalPluginProvider::id)
    private val pluginIdPattern = Regex("[A-Za-z0-9_.-]+")

    private val configFile get() = File(PluginPortalBase.plugin.dataFolder, "external-plugins.yml")
    private val stateFile get() = File(PluginPortalBase.plugin.dataFolder, "external-plugins-state.json")

    @Volatile
    private var configs: Map<String, ExternalPluginConfig> = emptyMap()
    private val states = mutableMapOf<String, ExternalPluginState>()

    val ids: List<String> get() = configs.keys.sorted()

    @Synchronized
    fun load(): List<String> {
        if (!configFile.exists()) PluginPortalBase.plugin.saveResource("external-plugins.yml", false)
        states.clear()
        states.putAll(readState())
        if (!stateFile.exists()) saveState()
        return reload()
    }

    @Synchronized
    fun reload(): List<String> {
        val errors = mutableListOf<String>()
        val yaml = YamlConfiguration()
        runCatching { yaml.load(configFile) }.onFailure {
            configs = emptyMap()
            return listOf("Could not read external-plugins.yml: ${it.message}")
        }

        val plugins = yaml.getConfigurationSection("plugins")
        if (plugins == null) {
            configs = emptyMap()
            return if (yaml.contains("plugins")) listOf("'plugins' must be a YAML section") else emptyList()
        }

        configs = plugins.getKeys(false).mapNotNull { id ->
            val section = plugins.getConfigurationSection(id)
            if (section == null) {
                errors += "$id: plugin entry must be a YAML section"
                return@mapNotNull null
            }

            val source = section.getString("source").orEmpty().trim()
            val provider = source.substringBefore(':').lowercase()
            val sourceId = source.substringAfter(':', "").trim()
            val file = section.getString("file").orEmpty().trim()
            val updatesValue = section.getString("updates", "manual").orEmpty()
            val updates = ExternalUpdatePolicy.from(updatesValue)

            val error = when {
                !pluginIdPattern.matches(id) -> "plugin ID may only contain letters, numbers, dots, underscores, and hyphens"
                provider !in providers -> "unsupported source provider '$provider'"
                sourceId.isBlank() -> "source must use the provider:value format"
                !isSafeJarName(file) -> "file must be a single .jar filename"
                updates == null -> "updates must be manual, auto, or disabled"
                else -> null
            }
            if (error != null) {
                errors += "$id: $error"
                return@mapNotNull null
            }

            id to ExternalPluginConfig(
                id = id,
                provider = provider,
                sourceId = sourceId,
                artifact = section.getString("artifact")?.trim()?.takeIf(String::isNotEmpty),
                asset = section.getString("asset")?.trim()?.takeIf(String::isNotEmpty),
                file = file,
                prereleases = section.getBoolean("prereleases", false),
                updates = updates!!
            )
        }.toMap()

        return errors
    }

    @Synchronized
    fun configuredPlugins(): List<Pair<ExternalPluginConfig, ExternalPluginState?>> =
        configs.values.sortedBy(ExternalPluginConfig::id).map { it to states[it.id] }

    @Synchronized
    fun check(id: String): ExternalPluginResult {
        val config = configs[id] ?: return failure("External plugin '$id' is not configured")
        val now = Instant.now().toString()
        return runCatching {
            val artifact = providers.getValue(config.provider).resolve(config)
            val previous = states[id]
            states[id] = (previous ?: emptyState(config)).copy(lastCheckedAt = now, lastError = null)
            saveState()
            ExternalPluginResult(
                success = true,
                message = if (previous?.version == null) {
                    "${config.id} is not installed; latest version is ${artifact.version}"
                } else if (previous.matches(artifact)) {
                    "${config.id} is up to date (${artifact.version})"
                } else {
                    "${config.id} has an update available: ${previous.version} -> ${artifact.version}"
                },
                artifact = artifact,
                updateAvailable = previous?.version != null && !previous.matches(artifact)
            )
        }.getOrElse { throwable ->
            val message = throwable.message ?: throwable::class.simpleName ?: "Unknown provider error"
            states[id] = (states[id] ?: emptyState(config)).copy(lastCheckedAt = now, lastError = message)
            saveState()
            failure("Could not check ${config.id}: $message")
        }
    }

    @Synchronized
    fun install(id: String): ExternalPluginResult {
        val config = configs[id] ?: return failure("External plugin '$id' is not configured")
        val destination = File(Constants.INSTALL_DIRECTORY, config.file)
        if (states[id]?.version != null) {
            if (destination.exists()) return failure("${config.id} is already installed; use external update")
            removeState(id)
        }
        if (destination.exists()) return failure("${destination.path} already exists and is not tracked as an external plugin")
        return download(config, destination)
    }

    @Synchronized
    fun update(id: String): ExternalPluginResult {
        val config = configs[id] ?: return failure("External plugin '$id' is not configured")
        if (config.updates == ExternalUpdatePolicy.DISABLED) return failure("${config.id} has updates disabled")
        if (states[id]?.version == null) return failure("${config.id} is not installed; use external install")

        val check = check(id)
        if (!check.success) return check
        if (!check.updateAvailable) return check
        return download(config, File(Constants.UPDATE_DIRECTORY, config.file), check.artifact)
    }

    @Synchronized
    fun invalidate(id: String): ExternalPluginResult {
        val config = configs[id] ?: return failure("External plugin '$id' is not configured")
        if (config.updates == ExternalUpdatePolicy.DISABLED) return failure("${config.id} has updates disabled")
        val state = states[id]?.takeIf { it.version != null }
            ?: return failure("${config.id} is not installed")

        states[id] = state.copy(invalidated = true)
        saveState()
        return ExternalPluginResult(
            success = true,
            message = "${config.id} was invalidated and will be downloaded by its next eligible update"
        )
    }

    @Synchronized
    fun updateAll(includeManual: Boolean): List<ExternalPluginResult> = configs.values
        .filter { it.updates == ExternalUpdatePolicy.AUTO || (includeManual && it.updates == ExternalUpdatePolicy.MANUAL) }
        .filter { states[it.id]?.version != null }
        .map { update(it.id) }

    private fun download(
        config: ExternalPluginConfig,
        destination: File,
        resolvedArtifact: ExternalArtifact? = null
    ): ExternalPluginResult {
        val artifact = resolvedArtifact ?: check(config.id).let {
            if (!it.success) return it
            it.artifact ?: return failure("Provider did not return an artifact for ${config.id}")
        }
        val temporaryFile = File.createTempFile("external-${config.id}-", ".jar", PluginPortalBase.plugin.dataFolder)

        try {
            val downloaded = download(URL(artifact.downloadUrl), temporaryFile, null)
                ?: return recordFailure(config, "Download failed")
            if (!isJar(downloaded)) return recordFailure(config, "Downloaded artifact is not a valid JAR")

            val actualHash = HashType.SHA256.hash(downloaded)
            if (artifact.sha256 != null && !actualHash.equals(artifact.sha256, ignoreCase = true)) {
                return recordFailure(config, "SHA-256 verification failed")
            }

            destination.parentFile.mkdirs()
            moveReplacing(downloaded, destination)
            val now = Instant.now().toString()
            states[config.id] = ExternalPluginState(
                provider = artifact.provider,
                source = artifact.sourceId,
                artifact = artifact.artifactId,
                version = artifact.version,
                build = artifact.build,
                sha256 = actualHash,
                file = relativePath(File(Constants.INSTALL_DIRECTORY, config.file)),
                installedAt = now,
                lastCheckedAt = now,
                lastError = null
            )
            saveState()
            return ExternalPluginResult(
                success = true,
                message = "${config.id} ${artifact.version} was downloaded to ${relativePath(destination)}",
                artifact = artifact,
                changed = true
            )
        } catch (throwable: Exception) {
            return recordFailure(config, throwable.message ?: throwable::class.simpleName ?: "Download failed")
        } finally {
            temporaryFile.delete()
        }
    }

    private fun recordFailure(config: ExternalPluginConfig, message: String): ExternalPluginResult {
        states[config.id] = (states[config.id] ?: emptyState(config)).copy(
            lastCheckedAt = Instant.now().toString(),
            lastError = message
        )
        saveState()
        return failure("Could not download ${config.id}: $message")
    }

    private fun readState(): Map<String, ExternalPluginState> {
        if (!stateFile.exists()) return emptyMap()
        return try {
            GSON.fromJson(stateFile.readText(), ExternalPluginStateFile::class.java)?.plugins.orEmpty()
        } catch (exception: Exception) {
            PluginPortalBase.plugin.logger.warning(
                "Could not read external-plugins-state.json; ignoring invalid state: ${exception.message}"
            )
            emptyMap()
        }
    }

    private fun removeState(id: String) {
        states.remove(id)
        saveState()
    }

    private fun saveState() {
        stateFile.parentFile.mkdirs()
        val temporaryFile = File(stateFile.parentFile, "${stateFile.name}.tmp")
        temporaryFile.writeText(GSON.toJson(ExternalPluginStateFile(states.toSortedMap())))
        moveReplacing(temporaryFile, stateFile)
    }

    private fun emptyState(config: ExternalPluginConfig) = ExternalPluginState(
        provider = config.provider,
        source = config.sourceId,
        artifact = config.artifact.orEmpty()
    )

    private fun isSafeJarName(name: String): Boolean = name.isNotBlank() &&
        name.endsWith(".jar", ignoreCase = true) &&
        !name.contains('/') && !name.contains('\\') && name != "." && name != ".."

    private fun isJar(file: File): Boolean = runCatching {
        ZipFile(file).use { it.entries().hasMoreElements() }
    }.getOrDefault(false)

    private fun moveReplacing(source: File, destination: File) {
        runCatching {
            Files.move(
                source.toPath(),
                destination.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        }.getOrElse {
            Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun relativePath(file: File): String {
        val root = File(".").canonicalFile.toPath()
        val path = file.canonicalFile.toPath()
        return if (path.startsWith(root)) root.relativize(path).toString().replace(File.separatorChar, '/') else file.path
    }

    private fun failure(message: String) = ExternalPluginResult(false, message)

    private data class ExternalPluginStateFile(
        val plugins: Map<String, ExternalPluginState> = emptyMap()
    )

}

data class ExternalPluginResult(
    val success: Boolean,
    val message: String,
    val artifact: ExternalArtifact? = null,
    val updateAvailable: Boolean = false,
    val changed: Boolean = false
)
