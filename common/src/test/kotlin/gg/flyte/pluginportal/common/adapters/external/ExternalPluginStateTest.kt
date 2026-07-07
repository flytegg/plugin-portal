package gg.flyte.pluginportal.common.adapters.external

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExternalPluginStateTest {
    private val artifact = ExternalArtifact(
        provider = "github",
        sourceId = "ViaVersion/ViaVersion",
        artifactId = "447429644",
        version = "5.10.0",
        build = null,
        filename = "ViaVersion-5.10.0.jar",
        downloadUrl = "https://github.com/example.jar",
        sha256 = "a".repeat(64),
        publishedAt = null,
        changelog = null
    )
    private val artifactState = ExternalArtifactState(
        artifactId = artifact.artifactId,
        version = artifact.version,
        build = artifact.build,
        sha256 = artifact.sha256!!,
        recordedAt = "2026-01-01T00:00:00Z"
    )

    @Test
    fun `staged artifact is not reported as installed`() {
        val state = ExternalPluginState(installed = null, staged = artifactState)

        assertFalse(state.matches(artifact))
        assertTrue(state.stagedMatches(artifact))
    }

    @Test
    fun `installed artifact matches independently from staged artifact`() {
        val state = ExternalPluginState(installed = artifactState)

        assertTrue(state.matches(artifact))
        assertFalse(state.stagedMatches(artifact))
    }

    @Test
    fun `config fingerprint change preserves state when installed jar still matches`() {
        val state = ExternalPluginState(configFingerprint = "old", installed = artifactState)

        val preserved = state.withConfigFingerprint("new", artifactState.sha256)

        assertEquals("new", preserved?.configFingerprint)
        assertEquals(artifactState, preserved?.installed)
    }

    @Test
    fun `config fingerprint change rejects state when installed jar does not match`() {
        val state = ExternalPluginState(configFingerprint = "old", installed = artifactState)

        assertNull(state.withConfigFingerprint("new", "b".repeat(64)))
    }
}
