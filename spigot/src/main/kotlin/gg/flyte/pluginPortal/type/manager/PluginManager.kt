package gg.flyte.pluginPortal.type.manager

import gg.flyte.common.api.plugins.schemas.MarketplacePlugin
import gg.flyte.common.util.pluginApiInterface
import gg.flyte.pluginPortal.type.manager.SpigotInstalledPluginLoader.addInstalledPlugin
import gg.flyte.pluginPortal.type.manager.SpigotInstalledPluginLoader.pluginFolder
import gg.flyte.pluginPortal.type.manager.SpigotInstalledPluginLoader.updateFolder
import gg.flyte.twilight.scheduler.async
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File

object PluginManager {

    fun installPlugin(plugin: MarketplacePlugin, after: (Boolean) -> Unit) {
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

    private fun MarketplacePlugin.getInstallDirectory() = if (isInstalled()) pluginFolder else updateFolder
}