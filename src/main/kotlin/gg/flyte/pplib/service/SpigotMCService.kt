package gg.flyte.pplib.service

/*
class SpigotMCService : PluginService {
    override fun getPlugin(id: String): MarketplacePlugin {
        val spigetTree = kotlin.runCatching {
            getJsonTree(getSpigetJSON(id))
        }.onFailure { throw PluginNotFoundException() }.getOrThrow()

        val iconUrl = spigetTree["icon"].get("url").asText()
        val imageUrl = if (iconUrl.isNullOrEmpty()) "https://cdn.discordapp.com/attachments/1065031876470906880/1105626560087736439/smallpreviewpluginportal.png" else "https://www.spigotmc.org/$iconUrl"

        var downloadURL: String = "https://api.spiget.org/v2/resources/$id/download"

        if (spigetTree["file"].get("externalUrl")?.asText() != null) {
            downloadURL = spigetTree["file"].get("externalUrl").asText() ?: ""

            if (!isJarFile(URL(downloadURL))) {
                downloadURL = getAPIPlugin(id).alternateDownload ?: ""
                if (downloadURL.isNullOrEmpty()) {
                    requestPlugin(RequestPlugin(
                        id,
                        MarketplaceService.SPIGOTMC,
                        "The plugin's download URL is not a direct download link.",
                        spigetTree["name"].asText(),
                        downloadURL,
                        ""
                    ))
                }
            }
        }

        var isPremium = false;
        runCatching {
            isPremium = spigetTree["premium"].asBoolean()
        }.onFailure { isPremium = false }


        return MarketplacePlugin(
            MarketplaceService.SPIGOTMC,
            spigetTree["id"].asText() ,
            spigetTree["name"].asText(),
            spigetTree["tag"].asText(),
            spigetTree["downloads"].asInt(),
            spigetTree["price"].asDouble(),
            spigetTree["rating"].get("average").asDouble(),
            imageUrl,
            spigetTree["versions"][0].get("id").asText(),
            downloadURL,
            isPremium,
        )
    }
}
*/