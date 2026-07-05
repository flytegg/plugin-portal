package gg.flyte.pluginportal.common.adapters.external

import java.time.Instant

data class ExternalArtifact(
    val provider: String,
    val sourceId: String,
    val artifactId: String,
    val version: String,
    val build: String?,
    val filename: String,
    val downloadUrl: String,
    val sha256: String?,
    val publishedAt: Instant?,
    val changelog: String?
)

data class ExternalPluginConfig(
    val id: String,
    val provider: String,
    val sourceId: String,
    val artifact: String?,
    val asset: String?,
    val file: String,
    val prereleases: Boolean,
    val updates: ExternalUpdatePolicy
)

enum class ExternalUpdatePolicy {
    MANUAL,
    AUTO,
    DISABLED;

    companion object {
        fun from(value: String) = entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}

data class ExternalArtifactState(
    val artifactId: String,
    val version: String,
    val build: String? = null,
    val sha256: String,
    val recordedAt: String
) {
    fun matches(artifact: ExternalArtifact): Boolean {
        if (artifactId != artifact.artifactId || version != artifact.version || build != artifact.build) return false
        return artifact.sha256 == null || sha256.equals(artifact.sha256, ignoreCase = true)
    }
}

data class ExternalPluginState(
    val configFingerprint: String? = null,
    val installed: ExternalArtifactState? = null,
    val staged: ExternalArtifactState? = null,
    val lastCheckedAt: String? = null,
    val lastError: String? = null,
    val invalidated: Boolean = false
) {
    fun matches(artifact: ExternalArtifact): Boolean {
        if (invalidated) return false
        return installed?.matches(artifact) == true
    }

    fun stagedMatches(artifact: ExternalArtifact): Boolean = !invalidated && staged?.matches(artifact) == true
}
