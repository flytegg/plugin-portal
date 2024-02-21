package gg.flyte.pluginportal.paper.plugin

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import gg.flyte.pluginportal.api.PluginPortalAPI
import gg.flyte.pluginportal.api.type.MarketplacePlugin
import gg.flyte.pluginportal.client.PPClient
import gg.flyte.pluginportal.extensions.getSha256Hash
import gg.flyte.pluginportal.paper.PluginPortal
import gg.flyte.pluginportal.paper.plugin.PPPluginCache.isInstalled
import gg.flyte.pluginportal.paper.plugin.PPPluginCache.pluginFolder
import gg.flyte.pluginportal.paper.plugin.PPPluginCache.updateFolder
import kotlinx.coroutines.withContext
import java.io.File

object PluginManager : PluginPortalAPI() {

    private fun MarketplacePlugin.getInstallDirectory() = if (isInstalled()) updateFolder else pluginFolder

    override suspend fun getPlugin(id: String): MarketplacePlugin? =
        PPPluginCache.getCachedPlugins().firstOrNull { it.id == id }.let {
            it ?: PPClient.getPluginById(id)
        }

    suspend fun searchForPlugins(query: String): HashSet<MarketplacePlugin> =
        PPClient.searchForPlugins(query).result.toHashSet()

    override suspend fun installPlugin(plugin: MarketplacePlugin, after: (Boolean) -> Unit) {
        withContext(PluginPortal.asyncContext) {
            PPClient.downloadFile(
                plugin.getLatestVersion()?.downloadUrl!!,
                File(plugin.getInstallDirectory(), "[PP] ${plugin.getUniqueName()}.jar")
            ) { success, file ->
                if (success) {
                    PPPluginCache.addInstalledPlugins(plugin.toCompactPlugin(file?.getSha256Hash()))
                    return@downloadFile after(true)
                } else {
                    PluginPortal.instance.logger.warning("Failed to install plugin ${plugin.getUniqueName()}")
                    return@downloadFile after(false)
                }
            }
        }

    }
}