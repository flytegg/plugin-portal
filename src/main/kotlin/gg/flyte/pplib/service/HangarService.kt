package gg.flyte.pplib.service

/*
class HangarService : PluginService {
    override fun getPlugin(id: String): MarketplacePlugin {
        val args = id.split(":")
        val plugin = HangarClient.getProject(args[0],args[1])
        return MarketplacePlugin(
            MarketplaceService.HANGAR,
            plugin.namespace.slug,
            plugin.name,
            plugin.description,
            plugin.stats.downloads,
            0.0,
            0.0,
            plugin.avatarUrl,
            plugin.getVersions().first().name,
            plugin.getLatestDownloadURL(),
            false,
        )
    }
}
 */