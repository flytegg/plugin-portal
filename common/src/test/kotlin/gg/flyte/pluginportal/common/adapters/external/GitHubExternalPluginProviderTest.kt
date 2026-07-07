package gg.flyte.pluginportal.common.adapters.external

import com.google.gson.JsonParser
import gg.flyte.pluginportal.common.adapters.external.providers.GitHubExternalPluginProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GitHubExternalPluginProviderTest {
    private val config = ExternalPluginConfig(
        id = "viaversion",
        provider = "github",
        sourceId = "ViaVersion/ViaVersion",
        artifact = null,
        asset = "ViaVersion-.*\\.jar",
        file = "ViaVersion.jar",
        prereleases = false,
        updates = ExternalUpdatePolicy.MANUAL
    )

    @Test
    fun `reads GitHub sha256 release asset digest`() {
        val sha256 = "a".repeat(64)
        val provider = providerWithAssetDigest("sha256:$sha256")

        assertEquals(sha256, provider.resolve(config).sha256)
    }

    @Test
    fun `allows release assets created before GitHub digests`() {
        val provider = providerWithAssetDigest(null)

        assertNull(provider.resolve(config).sha256)
    }

    @Test
    fun `rejects malformed GitHub release asset digest`() {
        val provider = providerWithAssetDigest("sha256:not-a-hash")

        assertFailsWith<ExternalPluginException> { provider.resolve(config) }
    }

    private fun providerWithAssetDigest(digest: String?): GitHubExternalPluginProvider {
        val digestProperty = digest?.let { ", \"digest\": \"$it\"" }.orEmpty()
        val response = JsonParser.parseString(
            """
            [{
              "tag_name": "5.10.0",
              "draft": false,
              "prerelease": false,
              "published_at": "2026-01-01T00:00:00Z",
              "body": "Changes",
              "assets": [{
                "id": 447429644,
                "name": "ViaVersion-5.10.0.jar",
                "browser_download_url": "https://github.com/example.jar"$digestProperty
              }]
            }]
            """.trimIndent()
        )
        return GitHubExternalPluginProvider(ExternalJsonClient { response })
    }
}
