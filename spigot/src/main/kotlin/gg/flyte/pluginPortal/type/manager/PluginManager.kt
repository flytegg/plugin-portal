package gg.flyte.pluginPortal.type.manager

import gg.flyte.common.api.plugins.schemas.InstalledPlugin
import gg.flyte.common.api.plugins.schemas.MarketplacePlugin
import gg.flyte.common.util.getHashes
import gg.flyte.common.util.pluginApiInterface
import gg.flyte.pluginPortal.type.manager.PPPluginCache.isInstalled
import gg.flyte.pluginPortal.type.manager.SpigotInstalledPluginLoader.pluginFolder
import gg.flyte.pluginPortal.type.manager.SpigotInstalledPluginLoader.updateFolder
import gg.flyte.twilight.scheduler.async
import java.io.File

object PluginManager {

    fun installPlugin(
        plugin: MarketplacePlugin,
        after: (Boolean) -> Unit
    ) {
        async {
            pluginApiInterface.downloadFile(plugin.getDownloadURL()!!)
                .execute().body()?.byteStream()
                .use { input ->
                    File(plugin.getInstallDirectory(), "[PP] ${plugin.getUniqueName()}.jar")
                        .also { file ->
                            file.outputStream()
                                .use { output ->
                                    input?.copyTo(output)
                                }.also { if (it == null) return@async after(false) }

                            addInstalledPlugin(
                                plugin,
                                plugin.versionData.latestVersion!!,
                                file
                            )
                        }

                }

            return@async after(true)
        }
    }

    private fun addInstalledPlugin(
        plugin: MarketplacePlugin,
        version: String,
        file: File,
    ) {
        PPPluginCache.removeInstalledPlugins(
            PPPluginCache.getInstalledPlugins()
                .firstOrNull { it.id == plugin.id } ?: return
        )

        PPPluginCache.addInstalledPlugins(
            with(plugin) {
                InstalledPlugin(
                    id,
                    displayInfo.name,
                    version,
                    file.getHashes(),
                )
            }
        )

        PPPluginCache.saveInstalledPlugins()
    }

    private fun MarketplacePlugin.getInstallDirectory() = if (isInstalled()) updateFolder else pluginFolder
}