package gg.flyte.pluginportal.common.adapters.external

import com.google.gson.JsonParser
import gg.flyte.pluginportal.common.adapters.external.providers.GeyserExternalPluginProvider
import gg.flyte.pluginportal.common.adapters.external.providers.GitHubExternalPluginProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExternalPluginProviderTest {
    @Test
    fun `github selects one asset from latest stable release`() {
        val provider = GitHubExternalPluginProvider(jsonClient(GITHUB_RELEASES))

        val artifact = provider.resolve(githubConfig())

        assertEquals("v5.4.0", artifact.version)
        assertEquals("101", artifact.artifactId)
        assertEquals("https://github.com/ViaVersion/ViaVersion/releases/download/v5.4.0/ViaVersion.jar", artifact.downloadUrl)
    }

    @Test
    fun `github rejects an ambiguous asset expression`() {
        val provider = GitHubExternalPluginProvider(jsonClient(GITHUB_RELEASES))

        val error = assertFailsWith<ExternalPluginException> {
            provider.resolve(githubConfig(asset = ".*\\.jar"))
        }

        assertEquals("Multiple release assets match '.*\\.jar'", error.message)
    }

    @Test
    fun `github includes prereleases only when configured`() {
        val provider = GitHubExternalPluginProvider(jsonClient(GITHUB_RELEASES))

        val artifact = provider.resolve(githubConfig(prereleases = true))

        assertEquals("v5.5.0-rc.1", artifact.version)
    }

    @Test
    fun `geyser resolves named download with checksum`() {
        val provider = GeyserExternalPluginProvider(jsonClient(GEYSER_BUILD))

        val artifact = provider.resolve(geyserConfig())

        assertEquals("2.2.5", artifact.version)
        assertEquals("138", artifact.build)
        assertEquals("abc123", artifact.sha256)
        assertEquals(
            "https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot",
            artifact.downloadUrl
        )
    }

    @Test
    fun `invalidated state does not match the same artifact`() {
        val artifact = GitHubExternalPluginProvider(jsonClient(GITHUB_RELEASES)).resolve(githubConfig())
        val state = ExternalPluginState(
            provider = artifact.provider,
            source = artifact.sourceId,
            artifact = artifact.artifactId,
            version = artifact.version,
            build = artifact.build,
            sha256 = artifact.sha256
        )

        assertTrue(state.matches(artifact))
        assertFalse(state.copy(invalidated = true).matches(artifact))
    }

    private fun githubConfig(
        asset: String = "^ViaVersion\\.jar$",
        prereleases: Boolean = false
    ) = ExternalPluginConfig(
        id = "viaversion",
        provider = "github",
        sourceId = "ViaVersion/ViaVersion",
        artifact = null,
        asset = asset,
        file = "ViaVersion.jar",
        prereleases = prereleases,
        updates = ExternalUpdatePolicy.MANUAL
    )

    private fun geyserConfig() = ExternalPluginConfig(
        id = "floodgate",
        provider = "geysermc",
        sourceId = "floodgate",
        artifact = "spigot",
        asset = null,
        file = "floodgate-spigot.jar",
        prereleases = false,
        updates = ExternalUpdatePolicy.MANUAL
    )

    private fun jsonClient(json: String) = ExternalJsonClient { JsonParser.parseString(json) }

    private companion object {
        val GITHUB_RELEASES = """
            [
              {
                "id": 12,
                "tag_name": "v5.5.0-rc.1",
                "prerelease": true,
                "draft": false,
                "published_at": "2026-07-03T10:00:00Z",
                "body": "Release candidate",
                "assets": [{"id": 201, "name": "ViaVersion.jar", "browser_download_url": "https://example.com/rc.jar"}]
              },
              {
                "id": 11,
                "tag_name": "v5.4.0",
                "prerelease": false,
                "draft": false,
                "published_at": "2026-07-01T10:00:00Z",
                "body": "Stable release",
                "assets": [
                  {"id": 101, "name": "ViaVersion.jar", "browser_download_url": "https://github.com/ViaVersion/ViaVersion/releases/download/v5.4.0/ViaVersion.jar"},
                  {"id": 102, "name": "ViaVersion-sources.jar", "browser_download_url": "https://example.com/sources.jar"}
                ]
              }
            ]
        """.trimIndent()

        val GEYSER_BUILD = """
            {
              "version": "2.2.5",
              "build": 138,
              "downloads": {
                "spigot": {
                  "name": "floodgate-spigot.jar",
                  "sha256": "abc123"
                }
              }
            }
        """.trimIndent()
    }
}
