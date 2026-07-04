package gg.flyte.pluginportal.common.adapters.external.providers

import com.google.gson.JsonObject
import gg.flyte.pluginportal.common.adapters.external.ExternalArtifact
import gg.flyte.pluginportal.common.adapters.external.ExternalJsonClient
import gg.flyte.pluginportal.common.adapters.external.ExternalPluginConfig
import gg.flyte.pluginportal.common.adapters.external.ExternalPluginException
import gg.flyte.pluginportal.common.adapters.external.ExternalPluginProvider
import gg.flyte.pluginportal.common.adapters.external.HttpExternalJsonClient
import java.time.Instant

internal class GitHubExternalPluginProvider(
    private val client: ExternalJsonClient = HttpExternalJsonClient
) : ExternalPluginProvider {
    override val id = "github"

    override fun resolve(config: ExternalPluginConfig): ExternalArtifact {
        if (!REPOSITORY.matches(config.sourceId)) {
            throw ExternalPluginException("GitHub source must use the owner/repository format")
        }
        val assetPattern = config.asset?.takeIf(String::isNotBlank)
            ?: throw ExternalPluginException("GitHub source requires an asset regex")
        val assetRegex = runCatching { assetPattern.toRegex() }
            .getOrElse { throw ExternalPluginException("Invalid GitHub asset regex: ${it.message}") }

        val (release, asset) = findReleaseAsset(config, assetRegex, assetPattern)
        val downloadUrl = asset.string("browser_download_url")
        if (!downloadUrl.startsWith("https://")) throw ExternalPluginException("GitHub returned an invalid asset URL")

        return ExternalArtifact(
            provider = id,
            sourceId = config.sourceId,
            artifactId = asset.string("id"),
            version = release.string("tag_name"),
            build = null,
            filename = asset.string("name"),
            downloadUrl = downloadUrl,
            sha256 = null,
            publishedAt = release.stringOrNull("published_at")?.let { runCatching { Instant.parse(it) }.getOrNull() },
            changelog = release.stringOrNull("body")
        )
    }

    private fun findReleaseAsset(
        config: ExternalPluginConfig,
        assetRegex: Regex,
        assetPattern: String
    ): Pair<JsonObject, JsonObject> {
        var page = 1
        var foundEligibleRelease = false

        while (true) {
            val response = client.get(
                "https://api.github.com/repos/${config.sourceId}/releases?per_page=$PAGE_SIZE&page=$page"
            )
            if (!response.isJsonArray) {
                throw ExternalPluginException("GitHub returned an unexpected releases response")
            }

            val releases = response.asJsonArray.map { element ->
                if (!element.isJsonObject) throw ExternalPluginException("GitHub returned an invalid release entry")
                element.asJsonObject
            }

            for (release in releases) {
                val draft = release.boolean("draft")
                val prerelease = release.boolean("prerelease")
                if (draft || (!config.prereleases && prerelease)) continue
                foundEligibleRelease = true

                val matches = release.matchingAssets(assetRegex)
                when {
                    matches.size == 1 -> return release to matches.single()
                    matches.size > 1 -> {
                        val tag = release.string("tag_name")
                        throw ExternalPluginException(
                            "Multiple release assets match '$assetPattern' in GitHub release $tag"
                        )
                    }
                }
            }

            if (releases.size < PAGE_SIZE) break
            page++
        }

        if (!foundEligibleRelease) {
            throw ExternalPluginException("GitHub has no matching release for ${config.sourceId}")
        }
        throw ExternalPluginException("No release asset matches '$assetPattern'")
    }

    private fun JsonObject.string(name: String): String = stringOrNull(name)
        ?: throw ExternalPluginException("GitHub response is missing '$name'")

    private fun JsonObject.stringOrNull(name: String): String? =
        get(name)?.takeUnless { it.isJsonNull }?.asString

    private fun JsonObject.boolean(name: String): Boolean =
        get(name)?.takeUnless { it.isJsonNull }?.asBoolean
            ?: throw ExternalPluginException("GitHub response is missing '$name'")

    private fun JsonObject.matchingAssets(assetRegex: Regex): List<JsonObject> {
        val assets = get("assets")
            ?: throw ExternalPluginException("GitHub response is missing 'assets'")
        if (!assets.isJsonArray) throw ExternalPluginException("GitHub returned an invalid assets response")

        return assets.asJsonArray.map { element ->
            if (!element.isJsonObject) throw ExternalPluginException("GitHub returned an invalid release asset")
            element.asJsonObject
        }.filter { assetRegex.matches(it.string("name")) }
    }

    private companion object {
        const val PAGE_SIZE = 100
        val REPOSITORY = Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")
    }
}
