package gg.flyte.pluginportal.common.managers

import gg.flyte.pluginportal.common.Constants
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.adapters.external.ExternalArtifact
import gg.flyte.pluginportal.common.adapters.external.ExternalArtifactState
import gg.flyte.pluginportal.common.adapters.external.ExternalPluginConfig
import gg.flyte.pluginportal.common.adapters.external.ExternalPluginProvider
import gg.flyte.pluginportal.common.adapters.external.ExternalPluginState
import gg.flyte.pluginportal.common.adapters.external.ExternalUpdatePolicy
import gg.flyte.pluginportal.common.adapters.external.providers.GeyserExternalPluginProvider
import gg.flyte.pluginportal.common.adapters.external.providers.GitHubExternalPluginProvider
import gg.flyte.pluginportal.common.notifications.DiscordWebhookNotifier
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.HashType
import gg.flyte.pluginportal.common.util.download
import org.bukkit.configuration.file.YamlConfiguration
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.MappingNode
import org.yaml.snakeyaml.nodes.ScalarNode
import java.io.File
import java.io.StringReader
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.time.Instant
import java.util.Locale
import java.util.concurrent.Executors
import java.util.zip.ZipFile

object ExternalPluginManager {
    private val providers: Map<String, ExternalPluginProvider> = listOf(
        GitHubExternalPluginProvider(),
        GeyserExternalPluginProvider()
    ).associateBy(ExternalPluginProvider::id)
    private val pluginIdPattern = Regex("[A-Za-z0-9_-]+")
    private val githubRepositoryPattern = Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")
    private val geyserIdentifierPattern = Regex("[A-Za-z0-9_-]+")
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "PluginPortal-External").apply { isDaemon = true }
    }

    private val configFile get() = File(PluginPortalBase.plugin.dataFolder, "external-plugins.yml")
    private val stateFile get() = File(PluginPortalBase.plugin.dataFolder, "external-plugins-state.json")

    @Volatile
    private var configs: Map<String, ExternalPluginConfig> = emptyMap()
    private val states = mutableMapOf<String, ExternalPluginState>()

    val ids: List<String> get() = configs.keys.sorted()

    fun executeAsync(action: () -> Unit) = executor.execute(action)

    fun close() = executor.shutdownNow()

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
        val yaml = YamlConfiguration().apply { options().pathSeparator('/') }
        val contents = runCatching { configFile.readText() }.getOrElse {
            configs = emptyMap()
            return listOf("Could not read external-plugins.yml: ${it.message}")
        }
        val duplicateIds = runCatching { duplicateExternalPluginIds(contents) }.getOrElse { emptySet() }
        duplicateIds.sorted().forEach { errors += "$it: plugin ID is specified more than once" }
        runCatching { yaml.loadFromString(contents) }.onFailure {
            configs = emptyMap()
            return listOf("Could not read external-plugins.yml: ${it.message}")
        }

        val plugins = yaml.getConfigurationSection("plugins")
        if (plugins == null) {
            configs = emptyMap()
            return if (yaml.contains("plugins")) listOf("'plugins' must be a YAML section") else emptyList()
        }

        val parsedConfigs = plugins.getKeys(false).mapNotNull { id ->
            if (id in duplicateIds) return@mapNotNull null
            if (!pluginIdPattern.matches(id)) {
                errors += "$id: plugin ID may only contain letters, numbers, underscores, and hyphens"
                return@mapNotNull null
            }

            val section = plugins.getConfigurationSection(id)
            if (section == null) {
                errors += "$id: plugin entry must be a YAML section"
                return@mapNotNull null
            }

            val source = section.getString("source").orEmpty().trim()
            val provider = source.substringBefore(':').lowercase()
            val sourceId = source.substringAfter(':', "").trim()
            val file = section.getString("file")?.trim()?.takeIf(String::isNotEmpty)
                ?: managedFileName(id, provider)
            val updatesValue = section.getString("updates", "manual").orEmpty()
            val updates = ExternalUpdatePolicy.from(updatesValue)
            val artifact = section.getString("artifact")?.trim()?.takeIf(String::isNotEmpty)
            val asset = section.getString("asset")?.trim()?.takeIf(String::isNotEmpty)
            val invalidAssetRegex = provider == "github" && asset != null && runCatching { asset.toRegex() }.isFailure

            val error = when {
                provider !in providers -> "unsupported source provider '$provider'"
                sourceId.isBlank() -> "source must use the provider:value format"
                !isSafeJarName(file) -> "file must be a single .jar filename"
                updates == null -> "updates must be manual, auto, or disabled"
                provider == "github" && asset == null -> "GitHub source requires an asset regex"
                invalidAssetRegex -> "GitHub asset must be a valid regex"
                provider == "geysermc" && artifact == null -> "GeyserMC source requires an artifact"
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
                artifact = artifact,
                asset = asset,
                file = file,
                prereleases = section.getBoolean("prereleases", false),
                updates = updates!!
            )
        }.toMap()

        val duplicateFileIds = parsedConfigs.values
            .groupBy { it.file.lowercase(Locale.ROOT) }
            .values
            .filter { it.size > 1 }
            .flatMap { duplicates ->
                val ids = duplicates.map(ExternalPluginConfig::id).sorted()
                duplicates.map { config ->
                    errors += "${config.id}: file '${config.file}' is also used by ${ids.filterNot { it == config.id }.joinToString()}"
                    config.id
                }
            }
            .toSet()

        configs = parsedConfigs.filterKeys { it !in duplicateFileIds }
        validateStateOwnership()
        removeMissingPluginStates()

        return errors
    }

    @Synchronized
    fun configuredPlugins(): List<Pair<ExternalPluginConfig, ExternalPluginState?>> =
        configs.values.sortedBy(ExternalPluginConfig::id).map { it to currentState(it) }

    @Synchronized
    fun managedHashes(): Set<String> = configs.keys
        .mapNotNull(states::get)
        .flatMap { state -> listOfNotNull(state.installed?.sha256, state.staged?.sha256) }
        .map(String::lowercase)
        .toSet()

    @Synchronized
    fun managedFileNames(): Set<String> = configs.values
        .map { it.file.lowercase(Locale.ROOT) }
        .toSet()

    @Synchronized
    fun verifyInstalledPluginStates() {
        var stateChanged = false
        val mismatchedIds = configs.values
            .mapNotNull { config ->
                val state = states[config.id] ?: return@mapNotNull null
                val pluginFile = File(Constants.INSTALL_DIRECTORY, config.file)
                if (!pluginFile.isFile) return@mapNotNull null

                val actualHash = runCatching { HashType.SHA256.hash(pluginFile) }
                    .onFailure {
                        PluginPortalBase.plugin.logger.warning(
                            "Could not verify external plugin '${config.id}': ${it.message ?: it::class.simpleName}"
                        )
                    }
                    .getOrNull()
                    ?: return@mapNotNull null

                when {
                    state.staged?.sha256.equals(actualHash, ignoreCase = true) -> {
                        states[config.id] = state.copy(
                            installed = state.staged,
                            staged = null,
                            invalidated = false
                        )
                        stateChanged = true
                        PluginPortalBase.plugin.logger.info(
                            "Applied staged external plugin update for '${config.id}'"
                        )
                        null
                    }
                    state.installed == null -> null
                    actualHash.equals(state.installed.sha256, ignoreCase = true) -> {
                        val stagedFile = File(Constants.UPDATE_DIRECTORY, config.file)
                        if (state.staged != null && !stagedFile.isFile) {
                            states[config.id] = state.copy(staged = null)
                            stateChanged = true
                            PluginPortalBase.plugin.logger.warning(
                                "Cleared missing staged external plugin update for '${config.id}'"
                            )
                        }
                        null
                    }
                    config.updates == ExternalUpdatePolicy.DISABLED -> null
                    else -> config.id
                }
            }

        mismatchedIds.forEach { id ->
            states[id] = states.getValue(id).copy(invalidated = true)
            stateChanged = true
            PluginPortalBase.plugin.logger.warning(
                "Invalidated external plugin state for '$id' because its configured JAR has changed"
            )
        }
        if (stateChanged) saveState()
    }

    @Synchronized
    fun check(id: String): ExternalPluginResult {
        val config = configs[id] ?: return failure("External plugin '$id' is not configured")
        val now = Instant.now().toString()
        return runCatching {
            val artifact = providers.getValue(config.provider).resolve(config)
            val previous = currentState(config)
            val installedMatches = previous?.matches(artifact) == true
            val stagedMatches = previous?.stagedMatches(artifact) == true
            states[id] = (previous ?: emptyState(config)).copy(lastCheckedAt = now, lastError = null)
            saveState()
            ExternalPluginResult(
                success = true,
                message = if (previous?.installed == null) {
                    "${config.id} is not installed; latest version is ${artifact.version}"
                } else if (stagedMatches) {
                    "${config.id} ${artifact.version} is staged and will be applied after restart"
                } else if (installedMatches) {
                    "${config.id} is up to date (${artifact.version})"
                } else {
                    "${config.id} has an update available: ${previous.installed.version} -> ${artifact.version}"
                },
                artifact = artifact,
                updateAvailable = previous?.installed != null && !installedMatches && !stagedMatches
            )
        }.getOrElse { throwable ->
            val message = throwable.message ?: throwable::class.simpleName ?: "Unknown provider error"
            states[id] = (states[id] ?: emptyState(config)).copy(lastCheckedAt = now, lastError = message)
            val stateError = runCatching { saveState() }.exceptionOrNull()
            val suffix = if (stateError == null) "" else "; state could not be persisted"
            failure("Could not check ${config.id}: $message$suffix")
        }
    }

    @Synchronized
    fun install(id: String): ExternalPluginResult {
        val config = configs[id] ?: return failure("External plugin '$id' is not configured")
        val destination = File(Constants.INSTALL_DIRECTORY, config.file)
        if (currentState(config)?.installed != null) {
            if (destination.exists()) return failure("${config.id} is already installed; use external update")
            removeState(id)
        }
        if (destination.exists()) return adoptMatchingInstalled(config, destination)
        return download(config, destination)
    }

    @Synchronized
    fun update(id: String): ExternalPluginResult {
        val config = configs[id] ?: return failure("External plugin '$id' is not configured")
        if (config.updates == ExternalUpdatePolicy.DISABLED) return failure("${config.id} has updates disabled")
        if (currentState(config)?.installed == null) return failure("${config.id} is not installed; use external install")

        val check = check(id)
        if (!check.success) return check
        if (!check.updateAvailable) return check
        val updateDirectory = Constants.UPDATE_DIRECTORY
        if (updateDirectory.canonicalFile == Constants.INSTALL_DIRECTORY.canonicalFile) {
            return failure("Bukkit's update folder resolves to the plugins folder; refusing to replace the loaded plugin")
        }
        return download(config, File(updateDirectory, config.file), check.artifact)
    }

    @Synchronized
    fun invalidate(id: String): ExternalPluginResult {
        val config = configs[id] ?: return failure("External plugin '$id' is not configured")
        if (config.updates == ExternalUpdatePolicy.DISABLED) return failure("${config.id} has updates disabled")
        val state = currentState(config)?.takeIf { it.installed != null }
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
        .filter { currentState(it)?.installed != null }
        .map { update(it.id) }

    @Synchronized
    fun addGitHub(
        id: String,
        repository: String,
        asset: String,
        prereleases: Boolean
    ): ExternalPluginResult = addConfig(gitHubConfig(id, repository, asset, managedFileName(id, "github"), prereleases))

    @Synchronized
    fun addGeyser(
        id: String,
        project: String,
        artifact: String
    ): ExternalPluginResult = addConfig(geyserConfig(id, project, artifact, managedFileName(id, "geysermc")))

    @Synchronized
    fun importGitHub(
        id: String,
        repository: String,
        asset: String,
        file: String,
        prereleases: Boolean
    ): ExternalPluginResult = importInstalled(gitHubConfig(id, repository, asset, file, prereleases))

    @Synchronized
    fun importGeyser(
        id: String,
        project: String,
        artifact: String,
        file: String
    ): ExternalPluginResult = importInstalled(geyserConfig(id, project, artifact, file))

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
            val previousState = states[config.id]
            val artifactState = ExternalArtifactState(
                artifactId = artifact.artifactId,
                version = artifact.version,
                build = artifact.build,
                sha256 = actualHash,
                recordedAt = now
            )
            val staged = destination.parentFile.canonicalFile == Constants.UPDATE_DIRECTORY.canonicalFile
            states[config.id] = (previousState ?: emptyState(config)).copy(
                configFingerprint = config.fingerprint(),
                installed = if (staged) previousState?.installed else artifactState,
                staged = if (staged) artifactState else null,
                lastCheckedAt = now,
                lastError = null,
                invalidated = false
            )
            try {
                saveState()
            } catch (exception: Exception) {
                if (previousState == null) states.remove(config.id) else states[config.id] = previousState
                if (!destination.delete()) {
                    PluginPortalBase.plugin.logger.warning(
                        "Could not remove untracked external plugin file ${destination.path} after state persistence failed"
                    )
                }
                throw exception
            }
            if (staged) {
                DiscordWebhookNotifier.externalPluginUpdated(config, previousState?.installed?.version, artifact)
            } else {
                DiscordWebhookNotifier.externalPluginInstalled(config, artifact)
            }
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
        val stateError = runCatching { saveState() }.exceptionOrNull()
        if (stateError != null) {
            PluginPortalBase.plugin.logger.warning(
                "Could not persist external plugin failure state: ${stateError.message ?: stateError::class.simpleName}"
            )
        }
        val suffix = if (stateError == null) "" else "; state could not be persisted"
        return failure("Could not download ${config.id}: $message$suffix")
    }

    private fun addConfig(config: ExternalPluginConfig): ExternalPluginResult {
        val validationError = validateConfig(config) ?: duplicateConfigError(config)
        if (validationError != null) return failure(validationError)

        writeConfig(config)
        val errors = reload()
        if (errors.isNotEmpty()) return failure("Configured ${config.id}, but external-plugins.yml has errors: ${errors.joinToString("; ")}")
        return ExternalPluginResult(
            success = true,
            message = "Configured ${config.id} in external-plugins.yml as ${config.file}; run /pp external install ${config.id} to download it"
        )
    }

    private fun gitHubConfig(
        id: String,
        repository: String,
        asset: String,
        file: String,
        prereleases: Boolean
    ) = ExternalPluginConfig(
        id = id,
        provider = "github",
        sourceId = repository,
        artifact = null,
        asset = normalizeGitHubAssetPattern(asset),
        file = file,
        prereleases = prereleases,
        updates = ExternalUpdatePolicy.MANUAL
    )

    private fun geyserConfig(
        id: String,
        project: String,
        artifact: String,
        file: String
    ) = ExternalPluginConfig(
        id = id,
        provider = "geysermc",
        sourceId = project,
        artifact = artifact,
        asset = null,
        file = file,
        prereleases = false,
        updates = ExternalUpdatePolicy.MANUAL
    )

    private fun importInstalled(config: ExternalPluginConfig): ExternalPluginResult {
        val validationError = validateConfig(config) ?: duplicateConfigError(config)
        if (validationError != null) return failure(validationError)

        val pluginFile = File(Constants.INSTALL_DIRECTORY, config.file)
        if (!pluginFile.isFile) return failure("${config.file} does not exist in ${relativePath(Constants.INSTALL_DIRECTORY)}")
        if (!isJar(pluginFile)) return failure("${config.file} is not a valid JAR")

        writeConfig(config)
        val errors = reload()
        if (errors.isNotEmpty()) return failure("Imported ${config.id}, but external-plugins.yml has errors: ${errors.joinToString("; ")}")

        return adoptInstalled(config, pluginFile, "Imported ${config.id} from ${config.file}")
    }

    private fun adoptInstalled(
        config: ExternalPluginConfig,
        pluginFile: File,
        successMessage: String = "Imported existing ${config.id} from ${relativePath(pluginFile)}",
        resolvedArtifact: ExternalArtifact? = null
    ): ExternalPluginResult {
        if (!pluginFile.isFile) return failure("${config.file} does not exist in ${relativePath(Constants.INSTALL_DIRECTORY)}")
        if (!isJar(pluginFile)) return failure("${config.file} is not a valid JAR")

        val hash = HashType.SHA256.hash(pluginFile)
        val artifact = resolvedArtifact ?: runCatching { providers.getValue(config.provider).resolve(config) }.getOrNull()
        val installed = artifactStateForInstalledFile(pluginFile, hash, artifact)

        states[config.id] = emptyState(config).copy(
            installed = installed,
            lastCheckedAt = Instant.now().toString(),
            lastError = null,
            invalidated = false
        )
        saveState()
        return ExternalPluginResult(
            success = true,
            message = successMessage
        )
    }

    private fun adoptMatchingInstalled(config: ExternalPluginConfig, pluginFile: File): ExternalPluginResult {
        if (!pluginFile.isFile) return failure("${config.file} does not exist in ${relativePath(Constants.INSTALL_DIRECTORY)}")
        if (!isJar(pluginFile)) return failure("${config.file} is not a valid JAR")

        val artifact = runCatching { providers.getValue(config.provider).resolve(config) }.getOrElse { throwable ->
            return failure("Could not verify existing ${config.id}: ${throwable.message ?: throwable::class.simpleName}")
        }
        val expectedHash = artifact.sha256
            ?: return failure("${relativePath(pluginFile)} already exists; provider did not supply a SHA-256 digest, so use /pp external import to track it explicitly")
        val actualHash = HashType.SHA256.hash(pluginFile)
        if (!actualHash.equals(expectedHash, ignoreCase = true)) {
            return failure("${relativePath(pluginFile)} already exists but does not match the latest provider artifact; remove it or use /pp external import to track the existing JAR")
        }

        return adoptInstalled(
            config,
            pluginFile,
            "Imported existing ${config.id} from ${relativePath(pluginFile)}",
            artifact
        )
    }

    private fun artifactStateForInstalledFile(
        pluginFile: File,
        hash: String,
        resolvedArtifact: ExternalArtifact?
    ): ExternalArtifactState {
        if (resolvedArtifact?.sha256?.equals(hash, ignoreCase = true) == true) {
            return ExternalArtifactState(
                artifactId = resolvedArtifact.artifactId,
                version = resolvedArtifact.version,
                build = resolvedArtifact.build,
                sha256 = hash,
                recordedAt = Instant.now().toString()
            )
        }

        return ExternalArtifactState(
            artifactId = "imported:${hash.take(12)}",
            version = readPluginVersion(pluginFile) ?: "imported",
            build = null,
            sha256 = hash,
            recordedAt = Instant.now().toString()
        )
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

    private fun currentState(config: ExternalPluginConfig): ExternalPluginState? {
        val state = states[config.id] ?: return null
        if (state.installed == null || File(Constants.INSTALL_DIRECTORY, config.file).isFile) return state

        states.remove(config.id)
        PluginPortalBase.plugin.logger.warning(
            "Removed stale external plugin state for '${config.id}' because its configured JAR is missing"
        )
        saveState()
        return null
    }

    private fun removeMissingPluginStates() {
        val staleIds = configs.values.mapNotNull { config ->
            val installed = states[config.id]?.installed != null
            val pluginFile = File(Constants.INSTALL_DIRECTORY, config.file)
            config.id.takeIf { installed && !pluginFile.isFile }
        }
        if (staleIds.isEmpty()) return

        staleIds.forEach { id ->
            states.remove(id)
            PluginPortalBase.plugin.logger.warning(
                "Removed stale external plugin state for '$id' because its configured JAR is missing"
            )
        }
        saveState()
    }

    private fun saveState() {
        stateFile.parentFile.mkdirs()
        val temporaryFile = File(stateFile.parentFile, "${stateFile.name}.tmp")
        temporaryFile.writeText(GSON.toJson(ExternalPluginStateFile(states.toSortedMap())))
        moveReplacing(temporaryFile, stateFile)
    }

    private fun emptyState(config: ExternalPluginConfig) = ExternalPluginState(
        configFingerprint = config.fingerprint()
    )

    private fun validateConfig(config: ExternalPluginConfig): String? {
        val invalidAssetRegex = config.provider == "github" &&
            config.asset != null &&
            runCatching { config.asset.toRegex() }.isFailure

        return when {
            !pluginIdPattern.matches(config.id) -> "${config.id}: plugin ID may only contain letters, numbers, underscores, and hyphens"
            config.provider !in providers -> "${config.id}: unsupported source provider '${config.provider}'"
            config.sourceId.isBlank() -> "${config.id}: source must not be blank"
            config.provider == "github" && !githubRepositoryPattern.matches(config.sourceId) -> "${config.id}: GitHub source must use the owner/repository format"
            config.provider == "geysermc" && !geyserIdentifierPattern.matches(config.sourceId) -> "${config.id}: invalid GeyserMC project name"
            !isSafeJarName(config.file) -> "${config.id}: file must be a single .jar filename"
            config.provider == "github" && config.asset.isNullOrBlank() -> "${config.id}: GitHub source requires an asset regex"
            invalidAssetRegex -> "${config.id}: GitHub asset must be a valid regex"
            config.provider == "geysermc" && config.artifact.isNullOrBlank() -> "${config.id}: GeyserMC source requires an artifact"
            config.provider == "geysermc" && !geyserIdentifierPattern.matches(config.artifact.orEmpty()) -> "${config.id}: invalid GeyserMC artifact name"
            else -> null
        }
    }

    private fun duplicateConfigError(config: ExternalPluginConfig): String? {
        if (config.id in configs) return "${config.id} is already configured"
        val duplicateFile = configs.values.firstOrNull { it.file.equals(config.file, ignoreCase = true) }
        return duplicateFile?.let { "${config.file} is already used by ${it.id}" }
    }

    private fun writeConfig(config: ExternalPluginConfig) {
        if (!configFile.exists()) PluginPortalBase.plugin.saveResource("external-plugins.yml", false)
        val yaml = YamlConfiguration.loadConfiguration(configFile).apply { options().pathSeparator('/') }
        val path = "plugins/${config.id}"
        yaml.set("$path/source", "${config.provider}:${config.sourceId}")
        yaml.set("$path/artifact", config.artifact)
        yaml.set("$path/asset", config.asset)
        yaml.set("$path/file", config.file)
        yaml.set("$path/prereleases", config.prereleases.takeIf { config.provider == "github" })
        yaml.set("$path/updates", config.updates.name.lowercase())
        yaml.save(configFile)
    }

    private fun validateStateOwnership() {
        var stateChanged = false

        configs.values.forEach { config ->
            val state = states[config.id] ?: return@forEach
            val fingerprint = config.fingerprint()
            if (state.configFingerprint == fingerprint) return@forEach

            val preservedState = state.withConfigFingerprint(fingerprint, installedFileHash(config))
            if (preservedState != null) {
                states[config.id] = preservedState
                stateChanged = true
                PluginPortalBase.plugin.logger.info(
                    "Preserved external plugin state for '${config.id}' after configuration changed"
                )
                return@forEach
            }

            states.remove(config.id)
            stateChanged = true
            PluginPortalBase.plugin.logger.warning(
                "Removed external plugin state for '${config.id}' because its configuration changed and the configured JAR does not match the saved installation"
            )
        }

        if (stateChanged) saveState()
    }

    private fun installedFileHash(config: ExternalPluginConfig): String? {
        val pluginFile = File(Constants.INSTALL_DIRECTORY, config.file)
        if (!pluginFile.isFile) return null
        return runCatching { HashType.SHA256.hash(pluginFile) }
            .onFailure {
                PluginPortalBase.plugin.logger.warning(
                    "Could not verify external plugin '${config.id}' after configuration changed: ${it.message ?: it::class.simpleName}"
                )
            }
            .getOrNull()
    }

    private fun ExternalPluginConfig.fingerprint(): String {
        val input = listOf(provider, sourceId, artifact, asset, file, prereleases.toString())
            .joinToString("\u0000") { it.orEmpty() }
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun isSafeJarName(name: String): Boolean = name.isNotBlank() &&
        name.endsWith(".jar", ignoreCase = true) &&
        !name.contains('/') && !name.contains('\\') && name != "." && name != ".."

    private fun isJar(file: File): Boolean = runCatching {
        ZipFile(file).use { it.entries().hasMoreElements() }
    }.getOrDefault(false)

    private fun readPluginVersion(file: File): String? = runCatching {
        ZipFile(file).use { zip ->
            val pluginYml = zip.getEntry("plugin.yml") ?: return@use null
            val contents = zip.getInputStream(pluginYml).bufferedReader().use { it.readText() }
            YamlConfiguration().apply { loadFromString(contents) }.getString("version")
        }
    }.getOrNull()

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

    private fun managedFileName(id: String, provider: String): String {
        val name = id.replace(Regex("[^A-Za-z0-9_.-]+"), "-").trim('-', '.', '_')
            .takeIf(String::isNotBlank)
            ?: "external"
        return "[PP] $name [${provider.uppercase(Locale.ROOT)}].jar"
    }

    private fun normalizeGitHubAssetPattern(asset: String): String {
        val trimmed = asset.trim()
        val hasRegexSyntax = trimmed.any { it in "^$.*+?[](){}|\\" }
        return if (hasRegexSyntax) trimmed else ".*${Regex.escape(trimmed)}.*\\.jar"
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

internal fun duplicateExternalPluginIds(contents: String): Set<String> {
    val root = Yaml().compose(StringReader(contents)) as? MappingNode ?: return emptySet()
    val plugins = root.value
        .firstOrNull { (it.keyNode as? ScalarNode)?.value == "plugins" }
        ?.valueNode as? MappingNode
        ?: return emptySet()

    return plugins.value
        .mapNotNull { (it.keyNode as? ScalarNode)?.value }
        .groupingBy { it }
        .eachCount()
        .filterValues { it > 1 }
        .keys
}
