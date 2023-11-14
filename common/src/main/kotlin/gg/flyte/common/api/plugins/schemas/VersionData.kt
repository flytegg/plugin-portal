package gg.flyte.common.api.plugins.schemas

data class VersionData(
    val releaseDate: String,
    val lastUpdated: String,
    val latestVersion: String?,
)
