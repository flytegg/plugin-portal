package gg.flyte.common.util

import gg.flyte.common.type.api.plugin.MarketplacePlugin
import java.io.File

fun installPlugin(
    plugin: MarketplacePlugin,
    url: String,
    pluginFolder: File,
    async: Boolean = false
): String { // Return SHA256 Hash
    val outputFile = File(pluginFolder, "${plugin.displayInfo.name} (PP-${plugin.id}).jar".replace(":", "~"))

    if (async) downloadFileAsync(url, outputFile) {}
    else downloadFileSync(url, outputFile)

    return outputFile.getSha256Hash()
}