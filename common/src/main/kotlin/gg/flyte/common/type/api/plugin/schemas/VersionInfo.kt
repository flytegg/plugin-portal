package gg.flyte.common.type.api.plugin.schemas

data class VersionInfo(
    val name: String,
    val downloadUrl: String?,
    val hashes: HashMap<String, String>,
    val releaseDate: String,
    val supportedVersionsRange: String?,
    val dependencies: HashSet<Dependency>? = hashSetOf(),
    val malwareInfo: MalwareInfo?,
)