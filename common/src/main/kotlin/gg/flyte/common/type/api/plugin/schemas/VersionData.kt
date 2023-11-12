package gg.flyte.common.type.api.plugin.schemas

data class VersionData(
    val releaseDate: String,
    val lastUpdated: String,
    val latestVersion: String?,
)
