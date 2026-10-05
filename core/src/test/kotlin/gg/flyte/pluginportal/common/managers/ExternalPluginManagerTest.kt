package gg.flyte.pluginportal.common.managers

import gg.flyte.pluginportal.common.adapters.external.ExternalArtifact
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExternalPluginManagerTest {
    @Test
    fun `builds stable managed filenames`() {
        assertEquals("[PP] viaversion [GITHUB].jar", managedExternalFileName("viaversion", "github"))
        assertEquals("[PP] floodgate [GEYSERMC].jar", managedExternalFileName("floodgate", "geysermc"))
    }

    @Test
    fun `turns a simple asset search into a jar regex`() {
        val pattern = normalizeExternalGitHubAssetPattern("ViaVersion").toRegex()

        assertTrue(pattern.matches("ViaVersion-5.4.2.jar"))
    }

    @Test
    fun `preserves an explicit asset regex`() {
        val pattern = "^ViaVersion-[0-9.]+\\.jar$"

        assertEquals(pattern, normalizeExternalGitHubAssetPattern(pattern))
    }

    @Test
    fun `uses verified provider metadata for a matching import`() {
        val hash = "a".repeat(64)
        val artifact = artifact(hash)

        val state = externalArtifactStateForInstalledFile(hash, "local-version", artifact, RECORDED_AT)

        assertEquals("asset-42", state.artifactId)
        assertEquals("v5.4.2", state.version)
        assertEquals(hash, state.sha256)
    }

    @Test
    fun `does not claim provider metadata when an import cannot be verified`() {
        val hash = "b".repeat(64)

        val state = externalArtifactStateForInstalledFile(hash, "5.3.0", artifact(null), RECORDED_AT)

        assertEquals("imported:${hash.take(12)}", state.artifactId)
        assertEquals("5.3.0", state.version)
        assertEquals(hash, state.sha256)
    }

    private fun artifact(hash: String?) = ExternalArtifact(
        provider = "github",
        sourceId = "ViaVersion/ViaVersion",
        artifactId = "asset-42",
        version = "v5.4.2",
        build = null,
        filename = "ViaVersion-5.4.2.jar",
        downloadUrl = "https://example.com/ViaVersion-5.4.2.jar",
        sha256 = hash,
        publishedAt = Instant.parse("2026-07-15T00:00:00Z"),
        changelog = null
    )

    private companion object {
        const val RECORDED_AT = "2026-07-15T00:00:00Z"
    }
}
