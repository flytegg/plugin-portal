package gg.flyte.pluginportal.plugin.adapters.platforms.github

import com.google.gson.annotations.SerializedName

data class GithubRelease(
    @SerializedName("tag_name")
    val tagName: String,
    val name: String,
    val prerelease: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("published_at")
    val publishedAt: String,

    val assets: List<GithubAsset>
)
