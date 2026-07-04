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

        val releases = client.get("https://api.github.com/repos/${config.sourceId}/releases")
        if (!releases.isJsonArray) throw ExternalPluginException("GitHub returned an unexpected releases response")

        val release = releases.asJsonArray
            .filter { it.isJsonObject }
            .map { it.asJsonObject }
            .firstOrNull {
                !it.boolean("draft") && (config.prereleases || !it.boolean("prerelease"))
            }
            ?: throw ExternalPluginException("GitHub has no matching release for ${config.sourceId}")

        val matches = release.getAsJsonArray("assets")
            ?.filter { it.isJsonObject }
            ?.map { it.asJsonObject }
            ?.filter { assetRegex.matches(it.string("name")) }
            .orEmpty()

        if (matches.isEmpty()) throw ExternalPluginException("No release asset matches '$assetPattern'")
        if (matches.size > 1) throw ExternalPluginException("Multiple release assets match '$assetPattern'")

        val asset = matches.single()
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

    private fun JsonObject.string(name: String): String = stringOrNull(name)
        ?: throw ExternalPluginException("GitHub response is missing '$name'")

    private fun JsonObject.stringOrNull(name: String): String? =
        get(name)?.takeUnless { it.isJsonNull }?.asString

    private fun JsonObject.boolean(name: String): Boolean =
        get(name)?.takeUnless { it.isJsonNull }?.asBoolean == true

    private companion object {
        val REPOSITORY = Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")
    }
}
