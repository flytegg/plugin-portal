package gg.flyte.pluginportal.plugin.adapters.platforms.github

import com.google.gson.annotations.SerializedName

data class GithubAsset(
    val id: Double,
    val name: String,

    @SerializedName("browser_download_url")
    val browserDownloadURL: String
)
