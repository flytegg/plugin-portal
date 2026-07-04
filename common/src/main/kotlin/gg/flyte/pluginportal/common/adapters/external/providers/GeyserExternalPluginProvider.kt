package gg.flyte.pluginportal.common.adapters.external.providers

import gg.flyte.pluginportal.common.adapters.external.ExternalArtifact
import gg.flyte.pluginportal.common.adapters.external.ExternalJsonClient
import gg.flyte.pluginportal.common.adapters.external.ExternalPluginConfig
import gg.flyte.pluginportal.common.adapters.external.ExternalPluginException
import gg.flyte.pluginportal.common.adapters.external.ExternalPluginProvider
import gg.flyte.pluginportal.common.adapters.external.HttpExternalJsonClient

internal class GeyserExternalPluginProvider(
    private val client: ExternalJsonClient = HttpExternalJsonClient
) : ExternalPluginProvider {
    override val id = "geysermc"

    override fun resolve(config: ExternalPluginConfig): ExternalArtifact {
        if (!IDENTIFIER.matches(config.sourceId)) throw ExternalPluginException("Invalid GeyserMC project name")
        val artifactId = config.artifact?.takeIf { IDENTIFIER.matches(it) }
            ?: throw ExternalPluginException("GeyserMC source requires a valid artifact")
        val endpoint = "https://download.geysermc.org/v2/projects/${config.sourceId}/versions/latest/builds/latest"
        val response = client.get(endpoint)
        if (!response.isJsonObject) throw ExternalPluginException("GeyserMC returned an unexpected build response")

        val build = response.asJsonObject
        val download = build.getAsJsonObject("downloads")?.getAsJsonObject(artifactId)
            ?: throw ExternalPluginException("GeyserMC build has no '$artifactId' artifact")
        val sha256 = download.get("sha256")?.takeUnless { it.isJsonNull }?.asString
            ?.takeIf(String::isNotBlank)
            ?: throw ExternalPluginException("GeyserMC artifact does not provide a SHA-256 hash")
        val version = build.get("version")?.takeUnless { it.isJsonNull }?.asString
            ?: throw ExternalPluginException("GeyserMC response is missing 'version'")
        val buildNumber = build.get("build")?.takeUnless { it.isJsonNull }?.asString
            ?: throw ExternalPluginException("GeyserMC response is missing 'build'")

        return ExternalArtifact(
            provider = id,
            sourceId = config.sourceId,
            artifactId = artifactId,
            version = version,
            build = buildNumber,
            filename = download.get("name")?.takeUnless { it.isJsonNull }?.asString ?: "$artifactId.jar",
            downloadUrl = "$endpoint/downloads/$artifactId",
            sha256 = sha256,
            publishedAt = null,
            changelog = null
        )
    }

    private companion object {
        val IDENTIFIER = Regex("[A-Za-z0-9_-]+")
    }
}
