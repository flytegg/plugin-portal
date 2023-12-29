package gg.flyte.pluginportal.manager

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import gg.flyte.pluginportal.PluginPortal
import gg.flyte.pluginportal.api.PluginPortalAPI
import gg.flyte.pluginportal.api.type.MarketplacePlugin
import gg.flyte.pluginportal.client.PPClient
import gg.flyte.pluginportal.manager.PPPluginCache.isInstalled
import gg.flyte.pluginportal.manager.PPPluginCache.pluginFolder
import gg.flyte.pluginportal.manager.PPPluginCache.updateFolder
import gg.flyte.twilight.Twilight
import kotlinx.coroutines.withContext
import java.io.File

object PluginManager : PluginPortalAPI() {

    private fun MarketplacePlugin.getInstallDirectory() = if (isInstalled()) updateFolder else pluginFolder

    override suspend fun getPlugin(id: String): MarketplacePlugin? =
        PPPluginCache.getCachedPlugins().firstOrNull { it.id == id }.let {
            it ?: PPClient.getPluginById(id)
        }


    override suspend fun searchForPlugins(query: String): HashSet<MarketplacePlugin> {
        return hashSetOf()
    }

    override suspend fun installPlugin(plugin: MarketplacePlugin, after: (Boolean) -> Unit) {
        PluginPortal.instance.asyncDispatch {
            PPClient.downloadFile(
                plugin.getLatestVersion()?.downloadUrl!!,
                File(plugin.getInstallDirectory(), "[PP] ${plugin.getUniqueName()}.jar")
            ) { success ->
                if (success) {
                    PPPluginCache.addInstalledPlugins(plugin.toCompactPlugin())
                    return@downloadFile after(true)
                } else {
                    Twilight.plugin.logger.warning("Failed to install plugin ${plugin.getUniqueName()}")
                    return@downloadFile after(true)
                }
            }
        }

    }
}