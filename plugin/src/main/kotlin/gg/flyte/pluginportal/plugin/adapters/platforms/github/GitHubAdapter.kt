package gg.flyte.pluginportal.plugin.adapters.platforms.github

import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.common.util.HashType
import gg.flyte.pluginportal.common.util.download
import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.adapters.Adaptation
import gg.flyte.pluginportal.plugin.adapters.AdapterPlugin
import gg.flyte.pluginportal.plugin.adapters.AdapterPluginCache
import gg.flyte.pluginportal.plugin.adapters.PlatformAdapter
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URL

object GitHubAdapter : PlatformAdapter {
    override fun download(adaptation: Adaptation) {
        val client = OkHttpClient()
        val response = client.newCall(
            Request.Builder()
                .url("https://api.github.com/repos/${adaptation.githubRepo}/releases")
                .build()
        ).execute()

        val json = response.body?.string() ?: return
        val githubReleases = GSON.fromJson(json, Array<GithubRelease>::class.java)

        val release = githubReleases
            .firstOrNull { release -> if (adaptation.githubPreRelease) true else !release.prerelease } ?: return



        // Check if we already have this version installed
        if (AdapterPluginCache.any { it.platformId == adaptation.githubRepo }) {
            PluginPortal.instance.logger.info("GitHub adapter ${adaptation.githubRepo} is already installed at ${release.name}")
            return
        }

        val asset = release.assets.firstOrNull { asset ->
            adaptation.githubNameFilter?.matches(asset.name) ?: true
        } ?: return

        PluginPortal.instance.logger.info("Downloading GitHub adapter ${adaptation.githubRepo} from ${release.name}")

        asset.download(adaptation, release)
    }

    fun GithubAsset.download(adaptation: Adaptation, release: GithubRelease): Boolean {

        val jarFile = File("plugins", "[PP] ${adaptation.githubRepo!!.split("/").last()} (ADAPTER-GITHUB).jar")

        val file = download(
            URL(browserDownloadURL),
            jarFile,
            null
        ) ?: return false

        AdapterPluginCache.removeIf { plugin -> plugin.platformId == name }

        AdapterPluginCache.add(
            AdapterPlugin(
                platformId = adaptation.githubRepo,
                name = adaptation.githubRepo.split("/").last(),
                version = release.name,
                platform = adaptation.platform,
                sha256 = HashType.SHA256.hash(file),
                sha512 = HashType.SHA512.hash(file),
                installedAt = System.currentTimeMillis(),
            )
        )

        AdapterPluginCache.save()

        return true
    }
}
