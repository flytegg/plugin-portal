package gg.flyte.pluginportal.api.type

data class PluginVersion(
    val name: String,
    val downloadUrl: String?,
    val hashes: HashMap<String, String>,
    val releaseDate: String,
    val supportedVersionsRange: String?,
    val dependencies: HashSet<Dependency>? = hashSetOf(),
    val malwareInfo: MalwareInfo?,
)