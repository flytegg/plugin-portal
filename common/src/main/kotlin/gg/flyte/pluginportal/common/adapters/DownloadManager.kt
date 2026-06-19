package gg.flyte.pluginportal.common.adapters

import gg.flyte.pluginportal.common.managers.LocalPluginCache

object DownloadManager {
    private val adapters = mutableListOf<DownloadAdapter>()
    
    init {
        // Register adapters in priority order
        registerAdapter(PolymartAdapter())
        registerAdapter(StandardMarketplaceAdapter())
        registerAdapter(CustomURLAdapter())
    }
    
    fun registerAdapter(adapter: DownloadAdapter) {
        adapters.add(adapter)
        adapters.sortByDescending { it.getPriority() }
    }
    
    fun download(request: DownloadRequest): DownloadResult {
        // Check if plugin is already installed (only for marketplace plugins)
        if (request.plugin != null) {
            val existingPlugin = LocalPluginCache.fromPlugin(request.plugin)
            if (existingPlugin != null) {
                return DownloadResult(
                    success = false,
                    error = "Plugin '${request.plugin.name}' (ID: ${request.plugin.id}) is already installed",
                    localPlugin = existingPlugin
                )
            }
        }
        
        // Find suitable adapter
        val adapter = adapters.firstOrNull { it.canHandle(request) }
            ?: return DownloadResult(false, error = "No suitable download adapter found")
        
        // Perform download
        val result = adapter.download(request)
        
        // If successful and has localPlugin info, add to cache
        // Note: Custom URL downloads won't have localPlugin and won't be cached
        if (result.success && result.localPlugin != null) {
            LocalPluginCache.add(result.localPlugin)
            LocalPluginCache.save()
        }
        
        return result
    }
}