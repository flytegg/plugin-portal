package gg.flyte.pluginportal.plugin.adapters

data class Adaptation(
    val name: String,
    val platform: AdapterPlatform,
    val trigger: AdapterTrigger,

    // GitHub
    val githubRepo: String? = null,
    val githubPreRelease: Boolean = false,
    val githubNameFilter: Regex? = null,

    // Modrinth
    val modrinthSlug: String? = null,
    val modrinthFeatured: Boolean = false,
    val modrinthPrimary: Boolean = false,
    val modrinthChannels: List<String> = emptyList(),
    val modrinthLoaders: List<String> = emptyList(),
)